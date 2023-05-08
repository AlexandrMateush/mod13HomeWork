package org.example;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsonPlaceholderApi  {


    private static final String API_BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final Gson gson = new Gson();

    public static User createUser(User user) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users")
                .header("Content-Type", "application/json")
                .requestBody(user.toString())
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
        JSONObject jsonObject = new JSONObject(response.body());
        User createdUser = new User();
        createdUser.setId(jsonObject.getInt("id"));
        createdUser.setName(jsonObject.getString("name"));
        createdUser.setUsername(jsonObject.getString("username"));
        createdUser.setEmail(jsonObject.getString("email"));
        return createdUser;
    }

    public static User updateUser(int id, User user) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users/" + id)
                .header("Content-Type", "application/json")
                .requestBody(user.toString())
                .method(Connection.Method.PUT)
                .ignoreContentType(true)
                .execute();
        JSONObject jsonObject = new JSONObject(response.body());
        User updatedUser = new User();
        updatedUser.setId(jsonObject.getInt("id"));
        updatedUser.setName(jsonObject.getString("name"));
        updatedUser.setUsername(jsonObject.getString("username"));
        updatedUser.setEmail(jsonObject.getString("email"));
        return updatedUser;
    }

    public static boolean deleteUser(int id) throws IOException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users/" + id)
                .method(Connection.Method.DELETE)
                .ignoreContentType(true)
                .execute();
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    public static List<User> getAllUsers() throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        JSONArray jsonArray = new JSONArray(response.body());
        List<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = new User();
            user.setId(jsonObject.getInt("id"));
            user.setName(jsonObject.getString("name"));
            user.setUsername(jsonObject.getString("username"));
            user.setEmail(jsonObject.getString("email"));
            users.add(user);
        }
        return users;
    }

    public static User getUserById(int id) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users/" + id)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        JSONObject jsonObject = new JSONObject(response.body());
        User user = new User();
        user.setId(jsonObject.getInt("id"));
        user.setName(jsonObject.getString("name"));
        user.setUsername(jsonObject.getString("username"));
        user.setEmail(jsonObject.getString("email"));
        return user;
    }

    public static User getUserByUsername(String username) throws IOException, JSONException {
        Connection.Response response = Jsoup.connect(API_BASE_URL + "/users")
                .data("username", username)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        JSONArray jsonArray = new JSONArray(response.body());
        if (jsonArray.length() == 0) {
            return null;
        }
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        User user = new User();
        user.setId(jsonObject.getInt("id"));
        user.setName(jsonObject.getString("name"));
        user.setUsername(jsonObject.getString("username"));
        user.setEmail(jsonObject.getString("email"));
        return user;
    }
    public static void getCommentsForLastPost(int userId, String fileName) throws IOException, JSONException {

        String url = String.format("https://jsonplaceholder.typicode.com/users/%d/posts", userId);
        JSONArray posts = new JSONArray(sendGetRequest(url));

        JSONObject lastPost = posts.getJSONObject(posts.length() - 1);
        int postId = lastPost.getInt("id");

        url = String.format("https://jsonplaceholder.typicode.com/posts/%d/comments", postId);
        JSONArray comments = new JSONArray(sendGetRequest(url));

        User userInfo = getUserInfo(userId);
        String filename = String.format("user-%d-post-%d-comments.json", userId, postId);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(comments.toString());
        writer.close();

        System.out.printf("Коментарі до останнього поста користувача %s (%s) збережено у файл %s%n",
                userInfo.getName(), userInfo.getEmail(), filename);
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

    public static User getUserInfo(int userId) throws IOException {
        String url = API_BASE_URL + "/users/" + userId;
        String response = sendGetRequest(url);
        return gson.fromJson(response, User.class);
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