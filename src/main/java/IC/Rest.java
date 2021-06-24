package IC;



import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;


import java.io.BufferedReader;
import java.io.InputStreamReader;




public class Rest {

    public Rest() {

    }





    public String doGetString(String command) {
        String lineOut = "";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpUriRequest httpUriRequest = new HttpGet(command);
            HttpResponse response = client.execute(httpUriRequest);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                lineOut=lineOut+line;
            }
        } catch (Exception e) {
            return "";
        }
        return lineOut;

    }

}




