package Main;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;


public class API
{
    final String USER_AGENT = "Mozilla/5.0";
    private String sessionID;

    API()
    {

    }


    public void login(String username, String password) {
        try {
            Connection con = Jsoup.connect(
                    "https://accounts.pixiv.net/login?lang=en&source=pc&view_type=page&ref=wwwtop_accounts_index");
            Connection.Response response = con.userAgent(USER_AGENT).execute();
            String postKey = response.parse().getElementsByAttributeValueContaining("name","post_key")
                    .attr("value");

            String postBody = "pixiv_id=" + username + "&password=" + password + "&post_key=" + postKey;
            sessionID = con.requestBody(postBody)
                    .userAgent(USER_AGENT)
                    .cookie("PHPSESSID", response.cookie("PHPSESSID"))
                    .method(Connection.Method.POST)
                    .execute()
                    .cookie("PHPSESSID");

            System.out.println(sessionID);
        }catch(IOException i) {

        }
    }

    public User getUser(String id)
    {
        return new User(sessionID,id);
    }


}
