import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SwmSchemaProbe {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            for (String table : new String[]{"system_dict_type", "system_dict_data", "js_sys_monitor_device_info", "swm_alarm_light", "swm_voice_template"}) {
                System.out.println("[" + table + "]");
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT column_name, data_type, is_nullable, column_default FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? ORDER BY ordinal_position")) {
                    statement.setString(1, table);
                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            System.out.println(result.getString(1) + "\t" + result.getString(2) +
                                    "\tnullable=" + result.getString(3) + "\tdefault=" + result.getString(4));
                        }
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT dict_type, COUNT(*) FROM system_dict_data " +
                    "WHERE dict_type IN ('warning_content_enum','warning_type_enum','hazard_category_enum'," +
                    "'is_patrol_included_enum','hazard_status_enum','external_personnel_enum','helmet_battery_enum') " +
                    "GROUP BY dict_type ORDER BY dict_type");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println("existing\t" + result.getString(1) + "\tcount=" + result.getLong(2));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() AND table_name LIKE '%monitor%device%' ORDER BY table_name");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println("monitor-table\t" + result.getString(1));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT request_url, exception_name, exception_message, exception_root_cause_message, exception_stack_trace " +
                    "FROM infra_api_error_log WHERE request_url LIKE '%/swm/helmet-device/export-excel%' " +
                    "ORDER BY id DESC LIMIT 3");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println("api-error\t" + result.getString(1) + "\t" + result.getString(2) +
                            "\t" + result.getString(3) + "\troot=" + result.getString(4) +
                            "\tstack=" + result.getString(5));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, permission, component FROM system_menu WHERE id = 901331");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println("menu-901331\t" + result.getString(1) + "\t" + result.getString(2) +
                            "\t" + result.getString(3));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, template_name, status, tenant_id, create_date, update_date " +
                    "FROM swm_voice_template WHERE template_name LIKE 'CODEX%' ORDER BY create_date DESC");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println("voice-audit\t" + result.getString("id") + "\t" +
                            result.getString("template_name") + "\tstatus=" + result.getString("status") +
                            "\ttenant=" + result.getString("tenant_id") + "\tcreated=" + result.getString("create_date") +
                            "\tupdated=" + result.getString("update_date"));
                }
            }
        }
    }
}
