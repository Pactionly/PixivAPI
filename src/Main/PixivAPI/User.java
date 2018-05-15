
package Main.PixivAPI;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class User
{
    private String sessionID;
    private boolean initialized;

    private String ID;
    private String name;
    private String url;
    private int bookmarkedUsersSize;
    private int worksSize;
    private int bookmarksSize;
    private int myPixivSize;


    public User(String sessionID, String ID)
    {
        this.sessionID = sessionID;
        this.ID = ID;
        this.url = "https://www.pixiv.net/member.php?id=" + ID;
        this.initialized = false;
    }

    /**
     * Compares two Users to see if they are equal. Two users are considered to be equal if they have the
     * same ID, even if other values don't match(Such as if they were initialized at different times)
     *
     * @param o The user to compare with "this" user
     * @return True if the users are equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        return o instanceof User && ((User) o).ID.equals(this.ID);
    }

    /**
     * Gets a page of users that this user has bookmarked.(Also known as following)
     * This function returns an empty vector if the requested page doesn't exist, and throws exceptions if the
     * object calling the function is invalid, or if the pageNumber given is less than one.
     * @param pageNumber The page of followed Users to get. Must be greater than 0.
     * @return A list of user objects that this user follows.
     * @throws RuntimeException If this object's ID or session are invalid.
     * @throws IllegalArgumentException If the pageNumber is less than 1.
     */
    public List<User> getBookmarkedUsers(int pageNumber)
    {
        if (pageNumber < 1)
        {
            throw new IllegalArgumentException("pageNumber cannot be less than 1");
        }
        if(!initialized)
        {
            initialize();
        }

        Vector<User> output = new Vector<>(48);

        Document page = getHTML("https://www.pixiv.net/bookmark.php?type=user&id=" + ID + "&p=" + pageNumber);
        Elements results = page.getElementById("search-result").child(0).child(0).children();
        String tempID;
        for (Element ele : results)
        {
            tempID = ele.child(0).attr("data-user_id");
            output.add(new User(sessionID, tempID));
        }

        return output;
    }

    /**
     * Gets the number of users that this user follows.
     * @return The number of users followed.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public int getBookmarkedUsersSize() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return bookmarkedUsersSize;
    }

    /**
     * Gets a page of works that this user has bookmarked.
     * This function returns an empty list if the requested page doesn't exist.
     * @param pageNumber The page of bookmarked works to get. Must be greater than 0.
     * @return A list of works that this user has bookmarked.
     * @throws RuntimeException If this object's ID or session are invalid.
     * @throws IllegalArgumentException If the pageNumber is less than 1.
     */
    public List<Work> getBookmarks(int pageNumber) throws RuntimeException
    {
        if(pageNumber < 1)
        {
            throw new IllegalArgumentException("pageNumber cannot be less than 1");
        }
        if(!initialized)
        {
            initialize();
        }

        Vector<Work> output = new Vector<>(20);

        Document page = getHTML("https://www.pixiv.net/bookmark.php?id=" + ID + "&p=" + pageNumber);
        Elements results = page.getElementsByClass("image-item");
        if(results.isEmpty())
        {
            return null;
        }
        String tempID;
        for (Element ele : results)
        {
            tempID = ele.child(0).attr("href");
            tempID = tempID.substring(tempID.indexOf("illust_id=") + 10);
            output.add(new Work(sessionID, tempID));
        }
        return output;
    }

    /**
     * Gets the number of works that this user has bookmarked.
     * @return The number of works bookmarked.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public int getBookmarksSize() throws  RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return bookmarksSize;
    }

    /**
     * Returns this user's ID.
     * This function does not validate the ID, therefore this function returning is not
     * a guarantee that a user with this ID actually exists.
     * @return The ID of the user.
     */
    public String getID()
    {
        return ID;
    }

    /**
     * Gets a page of works that this user has created.
     * Returns empty list if the page doesn't exist.
     * @param pageNumber The page of works to get. Must be greater than 0.
     * @return A list of works by this user.
     * @throws RuntimeException If this object's ID or session are invalid.
     * @throws IllegalArgumentException If pageNumber is less than 1.
     */
    public List<Work> getWorks(int pageNumber) throws RuntimeException
    {
        if(pageNumber < 1)
        {
            throw new IllegalArgumentException("pageNumber cannot be less than 1");
        }
        if(!initialized)
        {
            initialize();
        }

        Vector<Work> output = new Vector<>(20);

        Document page = getHTML("https://www.pixiv.net/member_illust.php?id=" + ID + "&p=" + pageNumber);
        Elements results = page.getElementsByClass("image-item");
        String tempID;
        for (Element ele : results)
        {
            tempID = ele.child(0).attr("href");
            tempID = tempID.substring(tempID.indexOf("illust_id=") + 10);
            output.add(new Work(sessionID, tempID));
        }
        return output;
    }

    /**
     * Gets the number of works that this user has created
     * @return The number of works by this user.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public int getWorksSize() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return worksSize;
    }

    /**
     * Gets the url of this user's profile
     * This function does not validate the ID of the user, which the URL is derived from.
     * Therefore this function returning does not guarantee that a user at the URL actually exists.
     * @return The profile url of the user.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Gets a page of this user's "My pixiv" users.
     * @param pageNumber The page of users to get. Must be greater than 0.
     * @return A list of users.
     * @throws RuntimeException If this object's ID or session are invalid.
     * @throws IllegalArgumentException If pageNumber is less than 1.
     */
    public List<User> getMyPixiv(int pageNumber) throws RuntimeException
    {
        if(pageNumber < 1)
        {
            throw new IllegalArgumentException("pageNumber cannot be less than 1");
        }
        if(!initialized)
        {
            initialize();
        }

        Vector<User> output = new Vector<>(48);

        Document page = getHTML("https://www.pixiv.net/mypixiv_all.php?id=" + ID + "&p=" + pageNumber);
        Elements results = page.getElementsByClass("member-item");
        String tempID;
        for (Element ele : results)
        {
            tempID = ele.child(0).attr("data-user_id");
            output.add(new User(sessionID, tempID));
        }

        return output;
    }

    /**
     * Gets the number of "My pixiv" friends this user has.
     * @return The number of my pixiv users.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public int getMyPixivSize() throws RuntimeException
    {
        if (!initialized)
        {
            initialize();
        }
        return myPixivSize;
    }

    /**
     * Gets this users username.
     * @return The username of this user.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public String getName() throws RuntimeException
    {
        if(!initialized)
        {
            initialize();
        }
        return name;
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

    /**
     * This function initializes fields and checks the the user ID and session ID are valid.
     * If they are not valid this function throws a runtime error, which can be caught.
     *
     * Because this function makes a connection over the internet, it's very slow to finish, therefore
     * it should only be called if there is a request for data that needs that connection, or if an ID needs to be
     * validated.
     * @throws RuntimeException If this object's ID or session are invalid.
     */
    public void initialize() throws RuntimeException //TODO Split into multiple functions reduce page loads
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

        // Sets name
        name = page.getElementsByClass("user-name").first().text();

        // Sets bookmarksSize
        page = getHTML("https://www.pixiv.net/bookmark.php?id=" + ID);
        ele = page.getElementsByClass("count-badge");

        temp = ele.first().text();
        temp = Pattern.compile("[^\\d]").matcher(temp).replaceAll("");
        bookmarksSize = Integer.parseInt(temp);


        // Sets worksSize
        page = getHTML("https://www.pixiv.net/member_illust.php?id=" + ID);
        ele = page.getElementsByClass("count-badge");

        temp = ele.first().text();
        temp = Pattern.compile("[^\\d]").matcher(temp).replaceAll("");
        worksSize = Integer.parseInt(temp);

        // Sets bookmarkedUsersSize
        page = getHTML("https://www.pixiv.net/bookmark.php?type=user&id=" + ID);
        ele = page.getElementsByClass("count-badge");

        temp = ele.first().text();
        bookmarkedUsersSize = Integer.parseInt(temp);

        // Sets myPixivSize
        page = getHTML("https://www.pixiv.net/mypixiv_all.php?id=" + ID);
        ele = page.getElementsByClass("count-badge");

        temp = ele.first().text();
        myPixivSize = Integer.parseInt(temp);

        // Marks completed
        initialized = true;
    }
}
