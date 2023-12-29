package nl.softworks.calendarAggregator.testing.impl.restApi;

import nl.softworks.calendarAggregator.testing.TestContext;
import org.apache.log4j.Logger;

public abstract class API {
    private static final Logger LOG = Logger.getLogger(API.class);

    protected String baseUrl() {
        return TestContext.get().configuration().webBaseUrl();
    }

    public static final String X_CLIENT_CERT_HEADER = "X-Client-Certificate";

    protected int postAsXml(String url, Object entity) {
//        try {
//            JAXBContext context = JAXBContext.newInstance(entity.getClass());
//            Marshaller mar = context.createMarshaller();
//            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            StringWriter stringWriter = new StringWriter();
//            mar.marshal(entity, stringWriter);
//
//            HttpClient httpClient = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.setHeader("Content-Type", ContentType.APPLICATION_XML.toString());
//            httpPost.setEntity(new StringEntity(stringWriter.toString()));
//            HttpResponse httpResponse = httpClient.execute(httpPost);
//            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            if (statusCode != 200) {
//                String bodyContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
//                LOG.error(bodyContent);
//            }
//            return statusCode;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        return 200;
    }
}
