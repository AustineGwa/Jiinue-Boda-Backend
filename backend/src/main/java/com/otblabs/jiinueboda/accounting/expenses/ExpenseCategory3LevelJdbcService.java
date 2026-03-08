package com.otblabs.jiinueboda.accounting.expenses;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseCategory3LevelJdbcService {

    private final JdbcTemplate jdbcTemplateOne;

    /**
     * Get full 3-level hierarchy as nested HashMap
     *
     * Returns: Map<Category, Map<Subcategory, List<MinorSubcategory>>>
     *
     * Example:
     * {
     *   "Administrative Expenses": {
     *     "Directors Expenses": ["Directors Facilitation", "Parents Weekly Pay", "Directors Welfare & Personal Expenses"],
     *     "Salaries & Wages": ["Basic Salaries", "Casual Wages & Mileage", "Employee Welfare & Benefits"],
     *     "Benevolent": [],  // No minor subcategories
     *     ...
     *   },
     *   "Marketing Expenses": {
     *     "Branded & Promotional Items": [],  // No minor subcategories
     *     ...
     *   }
     * }
     *
     * @return 3-level nested HashMap
     */
    public Map<String, Map<String, List<String>>> getFullHierarchy() {

        String sql = """
            
                SELECT
                            ec.category_name,
                            es.subcategory_name,
                            es.has_minor_subcategories,
                            ems.minor_subcategory_name
                        FROM expense_categories_2 ec
                        LEFT JOIN expense_subcategories_2 es ON ec.id = es.category_id
                        LEFT JOIN expense_minor_subcategories ems ON es.id = ems.subcategory_id
                        WHERE ec.is_active = true
                        AND (es.is_active = true OR es.is_active IS NULL)
                        AND (ems.is_active = true OR ems.is_active IS NULL)
                        ORDER BY ec.display_order ASC, es.display_order ASC, ems.display_order ASC
            """;

        Map<String, Map<String, List<String>>> hierarchy = new LinkedHashMap<>();

        jdbcTemplateOne.query(sql, (ResultSet rs) -> {
            String categoryName = rs.getString("category_name");
            String subcategoryName = rs.getString("subcategory_name");
            String minorSubcategoryName = rs.getString("minor_subcategory_name");

            // Initialize category if not exists
            hierarchy.putIfAbsent(categoryName, new LinkedHashMap<>());

            if (subcategoryName != null) {
                // Initialize subcategory if not exists
                hierarchy.get(categoryName).putIfAbsent(subcategoryName, new ArrayList<>());

                // Add minor subcategory if it exists
                if (minorSubcategoryName != null) {
                    hierarchy.get(categoryName).get(subcategoryName).add(minorSubcategoryName);
                }
            }
        });
        return hierarchy;
    }

    /**
     * Get full hierarchy with codes
     *
     * Returns: Map<CategoryCode-CategoryName, Map<SubcategoryCode-SubcategoryName, Map<MinorCode, MinorName>>>
     *
     * Example:
     * {
     *   "ADMIN-Administrative Expenses": {
     *     "ADM-DIR-Directors Expenses": {
     *       "ADM-DIR-FAC": "Directors Facilitation",
     *       "ADM-DIR-PWP": "Parents Weekly Pay",
     *       "ADM-DIR-WEL": "Directors Welfare & Personal Expenses"
     *     },
     *     "ADM-BEN-Benevolent": {}  // No minor subcategories
     *   }
     * }
     */
    public Map<String, Map<String, Map<String, String>>> getFullHierarchyWithCodes() {

        String sql = """
           
                SELECT
                           ec.category_code,
                           ec.category_name,
                           es.subcategory_code,
                           es.subcategory_name,
                           ems.minor_subcategory_code,
                           ems.minor_subcategory_name
                       FROM expense_categories_2 ec
                       LEFT JOIN expense_subcategories_2 es ON ec.id = es.category_id
                       LEFT JOIN expense_minor_subcategories ems ON es.id = ems.subcategory_id
                       WHERE ec.is_active = true
                       AND (es.is_active = true OR es.is_active IS NULL)
                       AND (ems.is_active = true OR ems.is_active IS NULL)
                       ORDER BY ec.display_order ASC, es.display_order ASC, ems.display_order ASC
            """;

        Map<String, Map<String, Map<String, String>>> hierarchy = new LinkedHashMap<>();

        jdbcTemplateOne.query(sql, (ResultSet rs) -> {
            String categoryCode = rs.getString("category_code");
            String categoryName = rs.getString("category_name");
            String subcategoryCode = rs.getString("subcategory_code");
            String subcategoryName = rs.getString("subcategory_name");
            String minorCode = rs.getString("minor_subcategory_code");
            String minorName = rs.getString("minor_subcategory_name");

            String categoryKey = categoryCode + "-" + categoryName;

            // Initialize category if not exists
            hierarchy.putIfAbsent(categoryKey, new LinkedHashMap<>());

            if (subcategoryCode != null && subcategoryName != null) {
                String subcategoryKey = subcategoryCode + "-" + subcategoryName;

                // Initialize subcategory if not exists
                hierarchy.get(categoryKey).putIfAbsent(subcategoryKey, new LinkedHashMap<>());

                // Add minor subcategory if it exists
                if (minorCode != null && minorName != null) {
                    hierarchy.get(categoryKey).get(subcategoryKey).put(minorCode, minorName);
                }
            }
        });

        return hierarchy;
    }

    /**
     * Get full hierarchy with complete details including IDs
     *
     * Returns: Map<String, Object> with nested structure
     */
    public Map<String, Map<String, Object>> getFullHierarchyWithDetails() {

        String sql = """
            
                SELECT
                            ec.id AS category_id,
                            ec.category_code,
                            ec.category_name,
                            ec.description AS category_description,
                            es.id AS subcategory_id,
                            es.subcategory_code,
                            es.subcategory_name,
                            es.description AS subcategory_description,
                            es.has_minor_subcategories,
                            ems.id AS minor_subcategory_id,
                            ems.minor_subcategory_code,
                            ems.minor_subcategory_name,
                            ems.description AS minor_subcategory_description
                        FROM expense_categories_2 ec
                        LEFT JOIN expense_subcategories_2 es ON ec.id = es.category_id
                        LEFT JOIN expense_minor_subcategories ems ON es.id = ems.subcategory_id
                        WHERE ec.is_active = true
                        AND (es.is_active = true OR es.is_active IS NULL)
                        AND (ems.is_active = true OR ems.is_active IS NULL)
                        ORDER BY ec.display_order ASC, es.display_order ASC, ems.display_order ASC
            """;

        Map<String, Map<String, Object>> categoryMap = new LinkedHashMap<>();
        Map<String, Map<String, Object>> subcategoryCache = new HashMap<>();

        jdbcTemplateOne.query(sql, (ResultSet rs) -> {
            String categoryName = rs.getString("category_name");
            Long subcategoryId = rs.getLong("subcategory_id");

            // Initialize category if not exists
            if (!categoryMap.containsKey(categoryName)) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("categoryId", rs.getLong("category_id"));
                categoryData.put("categoryCode", rs.getString("category_code"));
                categoryData.put("categoryDescription", rs.getString("category_description"));
                categoryData.put("subcategories", new ArrayList<Map<String, Object>>());

                categoryMap.put(categoryName, categoryData);
            }

            // Handle subcategory
            if (!rs.wasNull() && subcategoryId != null) {
                String subcategoryKey = categoryName + "-" + subcategoryId;

                if (!subcategoryCache.containsKey(subcategoryKey)) {
                    Map<String, Object> subcategoryData = new HashMap<>();
                    subcategoryData.put("id", subcategoryId);
                    subcategoryData.put("code", rs.getString("subcategory_code"));
                    subcategoryData.put("name", rs.getString("subcategory_name"));
                    subcategoryData.put("description", rs.getString("subcategory_description"));
                    subcategoryData.put("hasMinorSubcategories", rs.getBoolean("has_minor_subcategories"));
                    subcategoryData.put("minorSubcategories", new ArrayList<Map<String, Object>>());

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> subcategories =
                            (List<Map<String, Object>>) categoryMap.get(categoryName).get("subcategories");
                    subcategories.add(subcategoryData);

                    subcategoryCache.put(subcategoryKey, subcategoryData);
                }

                // Handle minor subcategory
                Long minorSubcategoryId = rs.getLong("minor_subcategory_id");
                if (!rs.wasNull() && minorSubcategoryId != null) {
                    Map<String, Object> minorSubcategoryData = new HashMap<>();
                    minorSubcategoryData.put("id", minorSubcategoryId);
                    minorSubcategoryData.put("code", rs.getString("minor_subcategory_code"));
                    minorSubcategoryData.put("name", rs.getString("minor_subcategory_name"));
                    minorSubcategoryData.put("description", rs.getString("minor_subcategory_description"));

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> minorSubcategories =
                            (List<Map<String, Object>>) subcategoryCache.get(subcategoryKey).get("minorSubcategories");
                    minorSubcategories.add(minorSubcategoryData);
                }
            }
        });

        return categoryMap;
    }

    /**
     * Get flattened list of all expense line items
     * Useful for dropdown where user needs to see all possible expense types
     *
     * Returns: List<Map<String, Object>> with each item containing full path
     *
     * Example:
     * [
     *   {
     *     "categoryName": "Administrative Expenses",
     *     "subcategoryName": "Directors Expenses",
     *     "minorSubcategoryName": "Directors Facilitation",
     *     "fullPath": "Administrative Expenses > Directors Expenses > Directors Facilitation",
     *     "categoryId": 1,
     *     "subcategoryId": 2,
     *     "minorSubcategoryId": 3
     *   }
     * ]
     */
    public List<Map<String, Object>> getFlattenedExpenseItems() {

        String sql = """
            
                SELECT
                            ec.id AS category_id,
                            ec.category_name,
                            es.id AS subcategory_id,
                            es.subcategory_name,
                            es.has_minor_subcategories,
                            ems.id AS minor_subcategory_id,
                            ems.minor_subcategory_name
                        FROM expense_categories_2 ec
                        LEFT JOIN expense_subcategories_2 es ON ec.id = es.category_id
                        LEFT JOIN expense_minor_subcategories ems ON es.id = ems.subcategory_id
                        WHERE ec.is_active = true
                        AND (es.is_active = true OR es.is_active IS NULL)
                        AND (ems.is_active = true OR ems.is_active IS NULL)
                        ORDER BY ec.display_order ASC, es.display_order ASC, ems.display_order ASC
            """;

        List<Map<String, Object>> items = new ArrayList<>();
        Set<String> addedSubcategories = new HashSet<>();

        jdbcTemplateOne.query(sql, (ResultSet rs) -> {
            Long categoryId = rs.getLong("category_id");
            String categoryName = rs.getString("category_name");
            Long subcategoryId = rs.getLong("subcategory_id");
            String subcategoryName = rs.getString("subcategory_name");
            boolean hasMinorSubcategories = rs.getBoolean("has_minor_subcategories");
            Long minorSubcategoryId = rs.getLong("minor_subcategory_id");
            String minorSubcategoryName = rs.getString("minor_subcategory_name");

            if (subcategoryName != null) {
                if (hasMinorSubcategories && minorSubcategoryName != null) {
                    // Add item with all 3 levels
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("categoryId", categoryId);
                    item.put("categoryName", categoryName);
                    item.put("subcategoryId", subcategoryId);
                    item.put("subcategoryName", subcategoryName);
                    item.put("minorSubcategoryId", minorSubcategoryId);
                    item.put("minorSubcategoryName", minorSubcategoryName);
                    item.put("fullPath", categoryName + " > " + subcategoryName + " > " + minorSubcategoryName);
                    item.put("level", 3);

                    items.add(item);
                } else if (!hasMinorSubcategories) {
                    // Add item with only 2 levels (no minor subcategories)
                    String subcategoryKey = categoryId + "-" + subcategoryId;
                    if (!addedSubcategories.contains(subcategoryKey)) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("categoryId", categoryId);
                        item.put("categoryName", categoryName);
                        item.put("subcategoryId", subcategoryId);
                        item.put("subcategoryName", subcategoryName);
                        item.put("minorSubcategoryId", null);
                        item.put("minorSubcategoryName", null);
                        item.put("fullPath", categoryName + " > " + subcategoryName);
                        item.put("level", 2);

                        items.add(item);
                        addedSubcategories.add(subcategoryKey);
                    }
                }
            }
        });
        return items;
    }

    /**
     * Get subcategories for a specific category (Level 2)
     */
    public List<String> getSubcategoriesByCategory(String categoryName) {

        String sql = """
            
                SELECT DISTINCT es.subcategory_name
                        FROM expense_subcategories_2 es
                        JOIN expense_categories_2 ec ON es.category_id = ec.id
                        WHERE ec.category_name = ?
                        AND ec.is_active = true
                        AND es.is_active = true
                        ORDER BY es.display_order ASC
            """;

        return jdbcTemplateOne.query(sql, (rs, rowNum) -> rs.getString("subcategory_name"), categoryName);
    }

    /**
     * Get minor subcategories for a specific subcategory (Level 3)
     */
    public List<String> getMinorSubcategoriesBySubcategory(String subcategoryName) {

        String sql = """
            SELECT ems.minor_subcategory_name
            FROM expense_minor_subcategories ems
            JOIN expense_subcategories_2 es ON ems.subcategory_id = es.id
            WHERE es.subcategory_name = ? 
            AND es.is_active = true 
            AND ems.is_active = true
            ORDER BY ems.display_order ASC
            """;

        return jdbcTemplateOne.query(sql, (rs, rowNum) -> rs.getString("minor_subcategory_name"), subcategoryName);
    }

    /**
     * Check if a subcategory has minor subcategories
     */
    public boolean hasMinorSubcategories(String subcategoryName) {
        String sql = """
            
                SELECT has_minor_subcategories
                        FROM expense_subcategories_2
                        WHERE subcategory_name = ?
                        AND is_active = true
            """;

        Boolean result = jdbcTemplateOne.queryForObject(sql, Boolean.class, subcategoryName);
        return result != null && result;
    }
}
