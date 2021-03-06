package repository;

import database.Column;
import database.MusicDatabase;
import model.JsonMappable;
import util.MusicLibraryRequestException;
import util.UrlUtil;

import javax.json.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class Repository<T> {

    public abstract String getTableName();

    public abstract Map<String, Column> getParamsToColumns();

    public abstract JsonMappable objectFromResultSet(ResultSet result);

    public JsonValue handleGet(HttpServletRequest request) throws MusicLibraryRequestException {
        String resourceId = UrlUtil.getPathSegment(request, 2);
        Map<String, String> queryParams = UrlUtil.getQueryParameters(request);

        if (resourceId == null) {
            if (queryParams.isEmpty()) {
                return getAllEntries();
            } else {
                return getFilteredEntries(queryParams);
            }
        } else {
            if (queryParams.isEmpty()) {
                try {
                    return getEntryWithId(Integer.valueOf(resourceId));
                } catch (NumberFormatException e) {
                    throw new MusicLibraryRequestException(HttpServletResponse.SC_NOT_FOUND, "Invalid resource id.");
                }
            } else {
                throw new MusicLibraryRequestException(HttpServletResponse.SC_BAD_REQUEST, "Cannot specify both resource id and query parameters.");
            }
        }
    }

    public void handlePost(HttpServletRequest request) throws MusicLibraryRequestException {
        String resourceId = UrlUtil.getPathSegment(request, 2);

        if (resourceId == null) {
            try {
                JsonReader reader = Json.createReader(request.getInputStream());
                JsonArray newEntries = reader.readArray();
                MusicDatabase.createEntries(this, newEntries);

            } catch (IOException | SQLException e) {
                e.printStackTrace();
                throw new MusicLibraryRequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }

        } else {
            throw new MusicLibraryRequestException(HttpServletResponse.SC_NOT_FOUND, "Cannot POST to resource");
        }
    }

    private JsonArray getAllEntries() throws MusicLibraryRequestException {
        JsonArrayBuilder builder = Json.createArrayBuilder();

        try {
            List<JsonMappable> entries = MusicDatabase.getAllEntries(this);
            for (JsonMappable entry : entries) {
                builder.add(entry.toJsonObjectBuilder());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MusicLibraryRequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return builder.build();
    }

    private JsonObject getEntryWithId(int resourceId) throws MusicLibraryRequestException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        try {
            JsonMappable entry = MusicDatabase.getEntryWithId(this, resourceId);

            if (entry == null) {
                throw new MusicLibraryRequestException(HttpServletResponse.SC_NOT_FOUND, "No resource found with given id.");
            }

            builder = entry.toJsonObjectBuilder();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MusicLibraryRequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return builder.build();
    }

    private JsonArray getFilteredEntries(Map<String, String> queryParams) throws MusicLibraryRequestException {
        JsonArrayBuilder builder = Json.createArrayBuilder();

        try {
            List<JsonMappable> entries = MusicDatabase.getFilteredEntries(this, queryParams);
            for (JsonMappable entry : entries) {
                builder.add(entry.toJsonObjectBuilder());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MusicLibraryRequestException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return builder.build();
    }
}
