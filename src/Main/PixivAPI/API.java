package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


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

    public Work getWork(String id)
    {
        return new Work(sessionID, id);
    }

    public List<Work> getDailyRankingWorks(int pageNumber, Calendar date) throws RuntimeException
    {
        org.jsoup.nodes.Document page = getHTML("https://www.pixiv.net/ranking.php?mode=daily&p=" + pageNumber);

        LinkedList<Work> output = new LinkedList<>();
        Elements result = page.getElementsByClass("ranking-item");
        String tempID;
        for(Element ele : result)
        {
           tempID =  ele.attr("data-id");
           output.add(new Work(sessionID, tempID));
        }
        return output;
    }

    public List<Work> getWeeklyRankingWorks(int pageNumber) throws RuntimeException
    {
        org.jsoup.nodes.Document page = getHTML("https://www.pixiv.net/ranking.php?mode=weekly&p=" + pageNumber);

        LinkedList<Work> output = new LinkedList<>();
        Elements result = page.getElementsByClass("ranking-item");
        String tempID;
        for(Element ele : result)
        {
            tempID =  ele.attr("data-id");
            output.add(new Work(sessionID, tempID));
        }
        return output;
    }

    public List<Work> getMonthlyRankingWorks(int pageNumber) throws  RuntimeException
    {
        org.jsoup.nodes.Document page = getHTML("https://www.pixiv.net/ranking.php?mode=monthly&p=" + pageNumber);

        LinkedList<Work> output = new LinkedList<>();
        Elements result = page.getElementsByClass("ranking-item");
        String tempID;
        for(Element ele : result)
        {
            tempID =  ele.attr("data-id");
            output.add(new Work(sessionID, tempID));
        }
        return output;
    }

    /**
     * Connects to a URL and returns a document of html.
     * Throws an error if the URL can't be connected to, or if connecting returns a bad request.
     * @param url The url to connect to.
     * @return A document of HTML.
     * @throws RuntimeException If the provided URL causes a bad request response.
     */
    private org.jsoup.nodes.Document getHTML(String url) throws RuntimeException
    {
        Connection connect = Jsoup.connect(url);
        org.jsoup.nodes.Document page;
        try
        {
            page = connect.cookie("PHPSESSID", sessionID).execute().parse();
        } catch (IOException e) {
            throw new RuntimeException("Bad Request");
        }
        return page;
    }


}
