package nl.softworks.calendarAggregator.infrastructure.http;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Turns the uploaded logo into an .ico file: the same picture at the sizes a browser tab, a bookmark bar
 * and a taskbar ask for.
 * <p>
 * Each entry holds PNG data, which every browser in use reads. That keeps us out of the BMP variant of the
 * format, which would require hand-writing the XOR image and its transparency mask.
 * <p>
 * Pure: bytes in, bytes out, so it can be unit tested without a database.
 */
public final class Favicon {

    /** The tab sizes (16, 32) and a size that survives a taskbar or a home screen. */
    private static final int[] SIZES = {16, 32, 48};

    private static final int TYPE_ICON = 1;
    private static final int PLANES = 1;
    private static final int BIT_COUNT = 32;
    private static final int HEADER_SIZE = 6;
    private static final int ENTRY_SIZE = 16;

    private Favicon() {
    }

    /**
     * @return the .ico file, or null when the image cannot be decoded, so the caller can answer 404
     */
    public static byte[] fromImage(byte[] image) {
        BufferedImage source = decode(image);
        return source == null ? null : ico(source);
    }

    /** @return the decoded image, or null when the bytes are not an image ImageIO reads, e.g. an svg */
    private static BufferedImage decode(byte[] image) {
        if (image == null || image.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(image));
        } catch (IOException e) {
            return null;
        }
    }

    private static byte[] ico(BufferedImage source) {
        byte[][] pngs = new byte[SIZES.length][];
        int totalSize = HEADER_SIZE + ENTRY_SIZE * SIZES.length;
        for (int sizeIndex = 0; sizeIndex < SIZES.length; sizeIndex++) {
            pngs[sizeIndex] = png(scale(source, SIZES[sizeIndex]));
            totalSize += pngs[sizeIndex].length;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(totalSize);
        writeShort(out, 0); // reserved
        writeShort(out, TYPE_ICON);
        writeShort(out, pngs.length);
        // the entries come first, so the offset of the first image is known before any image is written
        int offset = HEADER_SIZE + ENTRY_SIZE * SIZES.length;
        for (int sizeIndex = 0; sizeIndex < SIZES.length; sizeIndex++) {
            out.write(SIZES[sizeIndex]); // width, 0 would mean 256
            out.write(SIZES[sizeIndex]); // height
            out.write(0); // palette size, 0 for a true colour image
            out.write(0); // reserved
            writeShort(out, PLANES);
            writeShort(out, BIT_COUNT);
            writeInt(out, pngs[sizeIndex].length);
            writeInt(out, offset);
            offset += pngs[sizeIndex].length;
        }
        for (byte[] png : pngs) {
            out.writeBytes(png);
        }
        return out.toByteArray();
    }

    /** Draws the logo on a square, transparent canvas, scaled to fit and centered. */
    private static BufferedImage scale(BufferedImage source, int side) {
        BufferedImage target = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double ratio = Math.min((double) side / source.getWidth(), (double) side / source.getHeight());
            int width = (int) Math.round(source.getWidth() * ratio);
            int height = (int) Math.round(source.getHeight() * ratio);
            graphics.drawImage(source, (side - width) / 2, (side - height) / 2, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private static byte[] png(BufferedImage image) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            // writing to memory does not fail
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    /** ICO stores its numbers little endian, the reverse of what DataOutputStream writes. */
    private static void writeShort(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        writeShort(out, value & 0xFFFF);
        writeShort(out, (value >> 16) & 0xFFFF);
    }
}
