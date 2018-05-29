package com.handen.membot;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.WallUploadResponse;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import com.vk.api.sdk.objects.wall.responses.PostResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Main {

    // id приложения 6302004
    static int APP_ID = 6302004;
    static String CLIENT_SECRET = "T2EJEms4KQ7MWd1yhTTb";
    static String code = "5316dfbc86699ab0d1";
    static String REDIRECT_URI = "https://oauth.vk.com/blank.html";
   // static int userId = 178355852;
    static int groupId = -159029958;

    static TransportClient transportClient;
    static VkApiClient vk;
    static UserActor actor;

    static String downloadPath = "C:\\";

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public static ArrayList<Public> publics = new ArrayList<Public>();
    static ArrayList<Post> posts = new ArrayList<Post>();

    static {
        publics.add(new Public(-460389, "БОРЩ"));
        publics.add(new Public(-147166906, "Вот это смешно"));
        publics.add(new Public(-13743007, "Мемасики -13743007"));
        publics.add(new Public(-103663851, "Мемасики -103663851"));
//        publics.add(new Public(-57846937, "MDK"));
        publics.add(new Public(-65596623, "FTP"));
        publics.add(new Public(-73598440, "ЩЕБЕСТАН"));
        publics.add(new Public(-45745333, "4ch"));
        publics.add(new Public(-112510790, "мемная папка"));
    }

    public static void main(String[] args) {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s, String s1, String s2) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s, String s1, String s2) throws CertificateException {

                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


            authorize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                addNewPosts();
                Collections.sort(posts, new Comparator<Post>() {
                    @Override
                    public int compare(Post p1, Post p2) {
                        long currentMillis = new Date().getTime();

                        long post1Millis = currentMillis - p1.postDate;
                        long post2Millis = currentMillis - p2.postDate;

                        double post1Value = (((p1.likes + 1) * 100) + ((p1.reposts + 1) * 500) / post1Millis);
                        double post2Value = (((p2.likes + 1) * 100) + ((p2.reposts + 1) * 500) / post2Millis);

                        p1.setValue(post1Value);
                        p2.setValue(post2Value);
                        if (post1Value > post2Value)
                            return -1;
                        else if (post2Value > post1Value)
                            return 1;
                        else
                            return 0;
                    }
                });
        //        System.out.println(posts);
                if (posts.size() != 0) {
                    System.out.println("Количество новых постов за данных период: " + posts.size());
                    Post chosenPost = posts.get(0);
                    //Скачиваем самый нужный пост
                    downloadImage(chosenPost.getImagePath());

                    File file = new File("C:\\mem.jpg");

                    PhotoUpload serverResponse = vk.photos().getWallUploadServer(actor).execute();
                    WallUploadResponse uploadResponse = vk.upload().photoWall(serverResponse.getUploadUrl(), file).execute();
                    List<Photo> photoList = vk.photos().saveWallPhoto(actor, uploadResponse.getPhoto())
                            .server(uploadResponse.getServer())
                            .hash(uploadResponse.getHash())
                            .execute();

                    Photo photo = photoList.get(0);
                    String attachId = "photo" + photo.getOwnerId() + "_" + photo.getId();

                    postNewPost(attachId, chosenPost.getTitle());
                    Date date = new Date();
                    String publicTitle = chosenPost.getPublic().getTitle();
                    System.out.println("Добавлен новый пост: " + chosenPost.getId() + " " + publicTitle + " " + simpleDateFormat.format(date));
                }
                Thread.sleep(1800000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void authorize() throws Exception {
        transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);

        String url = "http://oauth.vk.com/authorize?client_id=" + APP_ID + "&display=page&redirect_uri=" + REDIRECT_URI + "&scope=106500&response_type=code&v=5.69";

        Desktop.getDesktop().browse(new URI(url));

        //      JOptionPane.showInputDialog("Please input access_token param from browser: ");

        Thread.sleep(15000);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        code = bufferedReader.readLine();

        UserAuthResponse authResponse = vk.oauth()
                .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
                .execute();

        actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
    }

    public static void downloadImage(String strURL) throws Exception {
        URL connection = new URL(strURL);
        HttpURLConnection urlconn;
        urlconn = (HttpURLConnection) connection.openConnection();
        urlconn.setRequestMethod("GET");
        urlconn.connect();
        InputStream in = null;
        in = urlconn.getInputStream();
        String myRandomName = "mem.jpg"; // Генерируйте любое удобное имя вместо хардкорного
        File file = new File(downloadPath + myRandomName);

        if (!file.exists()) {
            //Создаем его.
            file.createNewFile();
        }

        OutputStream writer = new FileOutputStream(file);
        byte buffer[] = new byte[127];
        int c = in.read(buffer);
        while (c > 0) {
            writer.write(buffer, 0, c);
            c = in.read(buffer);
        }
        writer.flush();
        writer.close();
        in.close();

    }

    public static void postNewPost(String attachId, String message) throws Exception {
        PostResponse getResponse = vk.wall().post(actor)
                .attachments(attachId)
                .fromGroup(true)
                .message(message)
                .ownerId(groupId)
                .execute();
    }

    public static void addNewPosts() throws Exception {
        posts.clear();
        for (Public p : publics) {
            if(actor == null) {
                System.out.println("actor is null");
                break;
            }
            GetResponse postResponse = vk.wall().get(actor)
                    .count(10)
                    .ownerId(p.getGroupId())
                    .offset(2)
                    .execute();

            JSONObject obj = new JSONObject(postResponse);
            JSONArray postArray = obj.getJSONArray("items");

            for (int i = postArray.length(); i > 0; i--) {
                JSONObject currentPost = postArray.getJSONObject(i - 1);
                int postDate = currentPost.getInt("date");
                if (postDate > p.getLastPostDate()) {
                    p.setLastPostDate(postDate);
                    checkAndAddPost(currentPost, p);
                }
            }
            Thread.sleep(500);
        }
    }

    public static void checkAndAddPost(JSONObject post, Public p) throws Exception {
        if (!post.get("postType").toString().equals("POST"))
            return;
        if (post.toString().contains("marked_as_ads"))
            if (post.getInt("marked_as_ads") == 1)
                return;
        if(post.getJSONArray("attachments") != null) {
            if (post.getJSONArray("attachments").length() == 1) {
                if (post.getJSONArray("attachments").getJSONObject(0).get("type").toString().equals("PHOTO")) {
                    posts.add(new Post(
                            post.getInt("id"),
                            post.getLong("date"),
                            post.getJSONObject("likes").getLong("count"),
                            post.getJSONObject("reposts").getLong("count"),
                            post.getString("text"),
                            getPostImagePath(post),
                            p
                    ));
                }
            }
        }
    }


    public static String getPostImagePath(JSONObject post) {
        Set<String> set = post.getJSONArray("attachments").getJSONObject(0).getJSONObject("photo").toMap().keySet();

        String max = "";

        int maxSum = 0;
        for (String s : set) {
            int currentSum = 0;
            if (s.contains("photo"))
                currentSum = getStringSum(s);
            if (currentSum > maxSum) {
                max = s;
                maxSum = currentSum;
            }
        }
        String path = post.getJSONArray("attachments").getJSONObject(0).getJSONObject("photo").toMap().get(max).toString();

        return path;
    }

    public static int getStringSum(String s) {
        int sum = 0;
        for (char c : s.toCharArray()) {
            sum += c;
        }
        return sum;
    }

    private static class Public {
        int groupId;
        int lastPostDate = -1;
        String title;

        public Public(int groupId, String title) {
            this.groupId = groupId;
            this.title = title;
        }

        public int getGroupId() {
            return groupId;
        }

        public int getLastPostDate() {
            return lastPostDate;
        }

        public void setLastPostDate(int lastPostDate) {
            this.lastPostDate = lastPostDate;
        }

        public String getTitle() {
            return title;
        }
    }

    private static class Post {
        int id;
        long postDate;
        long likes;
        long reposts;
        String title;
        String imagePath;
        Public p;
        double value;

        public Post(int id, long postDate, long likes, long reposts, String title, String imagePath, Public p) {
            this.id = id;
            this.postDate = postDate;
            this.likes = likes;
            this.reposts = reposts;
            this.title = title;
            this.imagePath = imagePath;
            this.p = p;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getTitle() {
            return title;
        }

        public int getId() {
            return id;
        }

        public Public getPublic() {
            return p;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

    }
}

/*
public static boolean compareImage(File fileA, File fileB) {
    try {
        // take buffer data from botm image files //
        BufferedImage biA = ImageIO.read(fileA);
        DataBuffer dbA = biA.getData().getDataBuffer();
        int sizeA = dbA.getSize();
        BufferedImage biB = ImageIO.read(fileB);
        DataBuffer dbB = biB.getData().getDataBuffer();
        int sizeB = dbB.getSize();
        // compare data-buffer objects //
        if(sizeA == sizeB) {
            for(int i=0; i<sizeA; i++) {
                if(dbA.getElem(i) != dbB.getElem(i)) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
    catch (Exception e) {
        System.out.println("Failed to compare image files ...");
        return  false;
    }
}
 */
