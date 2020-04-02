package cookie;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SlackReporter {

    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String SLACK_URL = System.getenv("SLACK_URL");

    public static void sendSimpleMessage(String text) throws Exception {
        JSONObject data = new JSONObject();
        System.out.println(text);
        data.put("text", text);
        send(data);
    }

    // HTTP Post request
    public static void send(JSONObject data) throws Exception {
        URL obj = new URL(SLACK_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic post request
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data.toString()); //("{\"text\": \"hello, world\"}");
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        String msg = con.getResponseMessage();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();
    }
}
