package com.store.erp.Repo;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class RepoUtils {
  
  protected boolean hasColumn(ResultSet rs, String columnName) {
      try {
          rs.findColumn(columnName);
          return true;
      } catch (SQLException e) {
          return false;
      }
  }

}
