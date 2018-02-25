package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Work
{
    private String sessionID;
    private boolean initialized;

    private String ID;
    private String url;
    private String authorID;
    private String authorName;
    private String caption;
    private String date;
    private List<String> imageURL;
    private WorkType type;
    private List<String> tags;
    private String title;
    private int views;
    private int likes;

    public Work(String sessionID, String ID)
    {
        this.sessionID = sessionID;
        this.ID = ID;
        url = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=" + ID;
        initialized = false;
    }

    /**
     * Compares two works to see if they are equal. Two works are considered to be equal if the have the
     * same ID even if other values are different.(Such as if they were initialized at different times)
     * @param o The work to compare with "this" work
     * @return True if the two works are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        return o instanceof Work && ((Work) o).ID.equals(this.ID);
    }

    /**
     * Returns a User object, representing the author of this work.
     * @return The user who created this work.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public User getAuthor() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return new User(sessionID, authorID);
    }

    /**
     * Returns the ID of the author of this work.
     * @return The ID of this works author.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public String getAuthorID() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return authorID;
    }

    /**
     * Returns the username of this works author.
     * @return A string containing this works author's username.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public String getAuthorName() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return authorName;
    }

    /**
     * Returns this works ID.
     * This function does not validate the ID, therefore this function returning is not
     * a guarantee that a work with this ID actually exists.
     * @return The ID of the work.
     */
    public String getID()
    {
        return ID;
    }

    /**
     * Returns a lists of URLs that direct link to the images of this work.
     * For Illustrations(Single image) this list contains a single element.
     * For Manga(Multiple images) this list contains as many elements as there are images.
     * For Ugoira(Animations) this list contains a single element, linking a zipped folder of all the
     * frames of the animation.
     * @return A linked list of image URLs
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public List<String> getImageURL() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return imageURL;
    }

    /**
     * Returns the type of this work.
     * Possible values:
     *   WorkType.ILLUSTRATION is for single images
     *   WorkType.MANGA is for multiple images
     *   WorkType.UGOIRA is for animations
     *   WorkType.UNKNOWN is for anything else
     *
     * @return A WorkType specifying the type of work this is.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public WorkType getType() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return type;
    }

    /**
     * Returns a linked list of the tags attached to this work.
     * @return A list of the tags of this work.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public List<String> getTags() throws RuntimeException
    {
        if(!initialized) {
            initialize();
        }
        return tags;
    }

    /**
     * Returns the title of this work.
     * @return The title of this work.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public String getTitle() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return title;
    }

    /**
     * Returns the date this work was published.
     * Format: (M)M/(D)D/YYYY HH:MM
     * Format notes, the hours are 24 Hour Time, and the months and days are not padded with leading zeros.
     * @return The date this work was published.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public String getDate() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return date;
    }

    /**
     * Returns the URL of this work's page. Note, to get a direct link to the image, the function getImageURL() is used.
     * This function does not validate the ID of the image, which the URL is derived from.
     * Therefore this function returning does not guarantee that a work at the URL actually exists.
     * @return The URL of this work's page.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Returns the number of views this work has.
     * @return The number of view the work has.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public int getViews() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return views;
    }

    /**
     * Returns the number of likes this work has.
     * @return The number of likes the work has.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public int getLikes() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return likes;
    }

    /**
     * Returns the caption text attached to the work.
     * This function does not include formatting that could exist in the text via HTML, such as line breaks,
     * bold or italics. The returned string includes only the raw text in the caption.
     * @return The caption text of the work.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public String getCaption() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return caption;
    }

    /**
     * Connects to a URL and returns a document of html.
     * Throws an error if the URL can't be connected to, or if connecting returns a bad request.
     * @param url The url to connect to.
     * @return A document of HTML.
     * @throws RuntimeException If the provided URL causes a bad request response.
     */
    private org.jsoup.nodes.Document getHTML(String url)
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

    /**
     * This function initialized fields and checks if the works ID and session are valid.
     * If they are not valid this function throws a runtime error which can be caught.
     *
     * Because this function makes a connection over the internet, it's very slow to finish, therefore
     * it should only be called if there is a request for data that needs that connection, or if an ID needs to be
     * validated.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public void initialize() throws RuntimeException
    {
        Elements ele;
        String temp;
        Document page = getHTML(url);

        // Checks User ID is valid
        ele = page.getElementsByClass("error-title");
        if(ele.size() != 0)
        {
            throw new RuntimeException("Bad User ID");
        }

        // Checks Session ID is valid
        ele = page.getElementsByClass("newindex-signup");
        if(ele.size() != 0)
        {
            throw new RuntimeException("Bad Session ID");
        }

        authorName = page.getElementsByClass("user-name").first().text();

        authorID =  page.getElementsByClass("user-name").attr("href").split("=")[1];

        caption = page.getElementsByClass("caption").first().text();

        date = page.getElementsMatchingOwnText("[1]?[0-9][/][1-3]?[0-9][/][0-9]{4}").first().text();

        //Find Type
        temp = page.getElementsByClass("thumbnail-container").first().child(0).className();
        switch(temp)
        {
            case "_work":
                type = WorkType.ILLUSTRATION;
                break;
            case "_work multiple":
                type = WorkType.MANGA;
                break;
            case "_work ugoku-illust":
                type = WorkType.UGOIRA;
                break;
            default:
                type = WorkType.UNKNOWN;
                break;
        }

        imageURL = new LinkedList<>();
        switch (type)
        {
            case ILLUSTRATION:
                imageURL.add(page.getElementsByClass("original-image").attr("data-src"));
                break;
            case MANGA:
                Document mangaPage = getHTML("https://www.pixiv.net/member_illust.php?mode=manga&illust_id=" + ID);
                ele = mangaPage.getElementsByAttribute("data-src");
                for(Element e: ele)
                {
                    imageURL.add(e.attr("data-src"));
                }
                break;
            case UGOIRA:
                 temp = page.html();
                Matcher m = Pattern.compile("IllustFullscreenData.+zip").matcher(temp);
                if(m.find())
                {
                    temp = m.group();
                    m = Pattern.compile("http.+zip").matcher(temp);
                    if(m.find())
                    {
                        temp = m.group();
                        m = Pattern.compile("\\\\").matcher(temp);
                        temp = m.replaceAll("");
                        imageURL.add(temp);
                    }
                    else
                    {
                        System.err.println("Error in reading ugoira url.");
                        imageURL = null;
                    }
                }
                else
                {
                    System.err.println("Error in reading ugoira url.");
                    imageURL = null;
                }
                break;
            default:
                break;
        }

        tags = new LinkedList<>();
        ele = page.getElementsByClass("text").select("a");
        for (Element e : ele)
        {
            tags.add(e.text());
        }

        title = page.getElementsByClass("title").get(1).text();

        views = Integer.parseInt(page.getElementsByClass("view-count").first().text());

        likes = Integer.parseInt(page.getElementsByClass("rated-count").first().text());

        initialized = true;
    }


}
