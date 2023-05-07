package org.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.HttpURLConnection;

import java.net.URL;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsonPlaceholderApi {

    private static final String API_BASE_URL = "https://jsonplaceholder.typicode.com";

    public static JSONObject createUser(JSONObject user) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users")
                .header("Content-Type", "application/json")
                .requestBody(user.toString())
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
        return new JSONObject(response.body());
    }

    public static JSONObject updateUser(int id, JSONObject user) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users/" + id)
                .header("Content-Type", "application/json")
                .requestBody(user.toString())
                .method(Connection.Method.PUT)
                .ignoreContentType(true)
                .execute();
        return new JSONObject(response.body());
    }

    public static boolean deleteUser(int id) throws IOException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users/" + id)
                .method(Connection.Method.DELETE)
                .ignoreContentType(true)
                .execute();
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    public static JSONObject getAllUsers() throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        return new JSONObject(response.body());
    }

    public static JSONObject getUserById(int id) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users/" + id)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        return new JSONObject(response.body());
    }

    public static JSONObject getUserByUsername(String username) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users")
                .data("username", username)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        return new JSONObject(response.body());
    }
    public static void getCommentsForLastPost(int userId, String fileName) throws IOException, JSONException {

        String url = String.format("https://jsonplaceholder.typicode.com/users/%d/posts", userId);
        JSONArray posts = new JSONArray(sendGetRequest(url));

        JSONObject lastPost = posts.getJSONObject(posts.length() - 1);
        int postId = lastPost.getInt("id");

        url = String.format("https://jsonplaceholder.typicode.com/posts/%d/comments", postId);
        JSONArray comments = new JSONArray(sendGetRequest(url));

        JSONObject userInfo = getUserInfo(userId);
        String filename = String.format("user-%d-post-%d-comments.json", userId, postId);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(comments.toString());
        writer.close();

        System.out.printf("Коментарі до останнього поста користувача %s (%s) збережено у файл %s%n",
                userInfo.getString("name"), userInfo.getString("email"), filename);
    }
    public static String sendGetRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
    public static JSONObject getUserInfo(int userId) throws IOException, JSONException {
        String url = "https://jsonplaceholder.typicode.com/users/" + userId;
        String response = sendGetRequest(url);
        JSONObject userInfo = new JSONObject(response);
        return userInfo;
    }
    public static void printOpenTasksForUser(int userId) {
        String url = "https://jsonplaceholder.typicode.com/users/" + userId + "/todos";

        try {
            Document doc = Jsoup.connect(url).get();
            Element table = doc.select("table").first();
            Elements rows = table.select("tr");
            String openTasks = "";
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                String completed = row.select("td").get(2).text();
                if (completed.equalsIgnoreCase("false")) {
                    String title = row.select("td").get(1).text();
                    openTasks += title + "\n";
                }
            }
            System.out.println("Відкриті задачі юзера " + userId + ":");
            System.out.println(openTasks);
            String fileName = "user-" + userId + "-open-tasks.txt";
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                fileWriter.write(openTasks);
            }
            System.out.println("Відкриті задачі юзера " + userId + " збереження до файлу " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}