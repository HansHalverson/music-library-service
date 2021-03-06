package repository;

import database.Column;
import model.Genre;
import model.JsonMappable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GenreRepository extends Repository<Genre> {

    private Map<String, Column> paramsToColumns;

    public GenreRepository() {
        paramsToColumns = new HashMap<>();
        paramsToColumns.put("name", new Column("name", Column.ColumnType.STRING));
        paramsToColumns.put("supergenreId", new Column("supergenre_id", Column.ColumnType.INT));
    }

    @Override
    public String getTableName() {
        return "genre";
    }

    @Override
    public Map<String, Column> getParamsToColumns() {
        return paramsToColumns;
    }

    @Override
    public JsonMappable objectFromResultSet(ResultSet resultSet) {
        try {
            int id = resultSet.getInt("genre_id");
            String name = resultSet.getString("name");
            int supergenreId = resultSet.getInt("supergenre_id");

            return new Genre(id, name, supergenreId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
