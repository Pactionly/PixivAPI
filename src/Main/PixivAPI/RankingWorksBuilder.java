package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class RankingWorksBuilder
{
    private final String sessionID;
    private String mode;
    private String content;
    private int pageNumber;
    private String date;

    RankingWorksBuilder(String sessionID)
    {
        this.sessionID = sessionID;
        mode = "daily";
        content = "";
        pageNumber = 1;
        date = "";
    }

    public RankingWorksBuilder mode(RankingType type)
    {
        switch (type)
        {
            case DAILY:
                mode = "daily";
                break;
            case WEEKLY:
                mode = "weekly";
                break;
            case MONTHLY:
                mode = "monthly";
                break;
            case ROOKIE:
                mode = "rookie";
                break;
            case ORIGINAL:
                mode = "original";
                break;
            case MALE:
                mode = "male";
                break;
            case FEMALE:
                mode = "female";
                break;
            case DAILY_R18:
                mode = "daily_r18";
                break;
            case WEEKLY_R18:
                mode = "weekly_r18";
                break;
            case MALE_R18:
                mode = "male_r18";
                break;
            case FEMALE_R18:
                mode = "female_r18";
                break;
            case R18G:
                mode = "r18g";
        }
        return this;
    }

    public RankingWorksBuilder content(WorkType type)
    {
        switch (type)
        {
            case OVERALL:
                content = "";
                break;
            case ILLUSTRATION:
                content = "illust";
                break;
            case UGOIRA:
                content = "ugoira";
                break;
            case MANGA:
                content = "manga";
                break;
        }
        return this;
    }

    public RankingWorksBuilder page(int page)
    {
        if(page < 1)
        {
            throw new IllegalArgumentException("Page cannot be less than 1");
        }
        pageNumber = page;
        return this;
    }

    public RankingWorksBuilder date(Calendar date)
    {
        this.date = String.format("%04d", date.get(Calendar.YEAR));
        this.date += String.format("%02d", date.get(Calendar.MONTH));
        this.date += String.format("%02d", date.get(Calendar.DAY_OF_MONTH));
        return this;
    }

    public List<Work> get()
    {
        Document html;
        Vector<Work> results;
        Connection connect;

        String url = "https://www.pixiv.net/ranking.php?mode=" + mode;
        if(!content.equals(""))
        {
            url += "&content=" + content;
        }
        url += "&p=" + pageNumber;
        if(!date.equals(""))
        {
            url += "&date=" + date;
        }

        try
        {
            connect = Jsoup.connect(url);
            html = connect.cookie("PHPSESSID", sessionID).execute().parse();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Bad Request");
        }

        results = new Vector<>(50);

        Elements images = html.getElementsByClass("ranking-image-item");
        String temp;
        for(Element ele : images)
        {
            temp = ele.child(0).attr("href");
            temp = temp.substring(temp.indexOf("illust_id=") + 10);
            results.add(new Work(sessionID, temp));
        }
        return results;
    }


}
