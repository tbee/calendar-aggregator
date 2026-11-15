package nl.softworks.calendarAggregator.infrastructure.http;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The logo becomes the favicon. Pure: an image in, the bytes of an .ico file out, so no browser and no
 * database are needed to check it.
 */
class FaviconTest {

    private static final int[] EXPECTED_SIZES = {16, 32, 48};

    /** The directory header: reserved 0, type 1 for an icon, then the number of entries. */
    @Test
    void theIconHoldsOnePngPerSizeABrowserAsksFor() throws IOException {
        byte[] ico = Favicon.fromImage(png(200, 100, Color.BLUE));

        assertNotNull(ico);
        ByteBuffer header = ByteBuffer.wrap(ico).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0, header.getShort());
        assertEquals(1, header.getShort());
        assertEquals(EXPECTED_SIZES.length, header.getShort(4) & 0xFFFF);

        List<BufferedImage> images = images(ico);
        assertEquals(EXPECTED_SIZES.length, images.size());
        for (int i = 0; i < EXPECTED_SIZES.length; i++) {
            assertEquals(EXPECTED_SIZES[i], images.get(i).getWidth());
            assertEquals(EXPECTED_SIZES[i], images.get(i).getHeight());
        }
    }

    /** Each entry's width and height byte must agree with the image behind it, and the offsets must not overlap. */
    @Test
    void theDirectoryDescribesTheImagesItPointsAt() throws IOException {
        byte[] ico = Favicon.fromImage(png(64, 64, Color.RED));

        ByteBuffer buffer = ByteBuffer.wrap(ico).order(ByteOrder.LITTLE_ENDIAN);
        int endOfPreviousImage = 6 + EXPECTED_SIZES.length * 16;
        for (int entry = 0; entry < EXPECTED_SIZES.length; entry++) {
            int entryStart = 6 + entry * 16;
            assertEquals(EXPECTED_SIZES[entry], buffer.get(entryStart) & 0xFF);
            assertEquals(EXPECTED_SIZES[entry], buffer.get(entryStart + 1) & 0xFF);
            assertEquals(1, buffer.getShort(entryStart + 4));
            assertEquals(32, buffer.getShort(entryStart + 6));
            int length = buffer.getInt(entryStart + 8);
            int offset = buffer.getInt(entryStart + 12);
            assertEquals(endOfPreviousImage, offset, "the images follow the directory without a gap");
            endOfPreviousImage = offset + length;
        }
        assertEquals(ico.length, endOfPreviousImage);
    }

    /** A wide logo keeps its shape: the square canvas is filled to the sides, with transparency above and below. */
    @Test
    void aNonSquareLogoIsFittedAndCentered() throws IOException {
        byte[] ico = Favicon.fromImage(png(200, 100, Color.GREEN));

        BufferedImage largest = images(ico).get(EXPECTED_SIZES.length - 1);
        assertEquals(48, largest.getWidth());
        assertEquals(Color.GREEN.getRGB() & 0xFFFFFF, largest.getRGB(24, 24) & 0xFFFFFF, "the middle is logo");
        assertEquals(0, largest.getRGB(24, 1) >>> 24, "the top row stays transparent");
    }

    /** The form also accepts an svg, and a broken upload should not become a corrupt icon. */
    @Test
    void bytesThatAreNotARasterImageYieldNoIcon() {
        assertNull(Favicon.fromImage("<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes()));
        assertNull(Favicon.fromImage(new byte[0]));
        assertNull(Favicon.fromImage(null));
    }

    // ==================================================================
    // HELPERS

    /** Decodes the icon the way a browser does: read the directory, then the PNG behind each offset. */
    private static List<BufferedImage> images(byte[] ico) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(ico).order(ByteOrder.LITTLE_ENDIAN);
        int count = buffer.getShort(4) & 0xFFFF;
        List<BufferedImage> images = new ArrayList<>(count);
        for (int entry = 0; entry < count; entry++) {
            int entryStart = 6 + entry * 16;
            int length = buffer.getInt(entryStart + 8);
            int offset = buffer.getInt(entryStart + 12);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(ico, offset, length));
            assertNotNull(image, "entry " + entry + " holds a png");
            images.add(image);
        }
        return images;
    }

    private static byte[] png(int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", out));
        return out.toByteArray();
    }
}
