package com.otblabs.jiinueboda.accounting.expenses;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/expenses")
public class ExpenseCategory3LevelController {

    private final ExpenseCategory3LevelJdbcService categoryService;

    public ExpenseCategory3LevelController(ExpenseCategory3LevelJdbcService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Get full 3-level hierarchy
     * Returns: Map<Category, Map<Subcategory, List<MinorSubcategory>>>
     *
     * Example response:
     * {
     *   "Administrative Expenses": {
     *     "Directors Expenses": ["Directors Facilitation", "Parents Weekly Pay", ...],
     *     "Benevolent": []
     *   }
     * }
     */
    @GetMapping("/expenses-categories/hierarchy")
    public ResponseEntity<Map<String, Map<String, List<String>>>> getFullHierarchy() {
        Map<String, Map<String, List<String>>> hierarchy = categoryService.getFullHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * Get 3-level hierarchy with codes

     * Returns: Map<CategoryKey, Map<SubcategoryKey, Map<MinorCode, MinorName>>>
     */
    @GetMapping("/expenses-categories/hierarchy-with-codes")
    public ResponseEntity<Map<String, Map<String, Map<String, String>>>> getHierarchyWithCodes() {
                Map<String, Map<String, Map<String, String>>> hierarchy =
                categoryService.getFullHierarchyWithCodes();
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * Get full hierarchy with complete details
     * Returns: Map<CategoryName, Map<String, Object>> with IDs, codes, descriptions
     */
    @GetMapping("/expenses-categories/full-hierarchy")
    public ResponseEntity<Map<String, Map<String, Object>>> getFullHierarchyWithDetails() {
        Map<String, Map<String, Object>> hierarchy = categoryService.getFullHierarchyWithDetails();
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * Get flattened list of all expense items
     * Perfect for a single searchable dropdown
     * Returns: List<Map<String, Object>> with full paths
     *
     * Example response:
     * [
     *   {
     *     "categoryId": 3,
     *     "categoryName": "Administrative Expenses",
     *     "subcategoryId": 7,
     *     "subcategoryName": "Directors Expenses",
     *     "minorSubcategoryId": 1,
     *     "minorSubcategoryName": "Directors Facilitation",
     *     "fullPath": "Administrative Expenses > Directors Expenses > Directors Facilitation",
     *     "level": 3
     *   }
     * ]
     */
    @GetMapping("/expenses-categories/flattened")
    public ResponseEntity<List<Map<String, Object>>> getFlattenedExpenseItems() {
        List<Map<String, Object>> items = categoryService.getFlattenedExpenseItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get subcategories for a specific category

     * Path variable: categoryName - Name of the category (e.g., "Administrative Expenses")
     *
     * Returns: List<String> of subcategory names
     */
    @GetMapping("/{categoryName}/subcategories")
    public ResponseEntity<List<String>> getSubcategoriesByCategory(
            @PathVariable String categoryName) {

        List<String> subcategories = categoryService.getSubcategoriesByCategory(categoryName);
        return ResponseEntity.ok(subcategories);
    }


    @GetMapping("/subcategory/{subcategoryName}/minor-subcategories")
    public ResponseEntity<List<String>> getMinorSubcategoriesBySubcategory(
            @PathVariable String subcategoryName) {

        List<String> minorSubcategories =
                categoryService.getMinorSubcategoriesBySubcategory(subcategoryName);
        return ResponseEntity.ok(minorSubcategories);
    }

    @GetMapping("/check-minor/{subcategoryName}")
    public ResponseEntity<Boolean> hasMinorSubcategories(
            @PathVariable String subcategoryName) {
        boolean hasMinor = categoryService.hasMinorSubcategories(subcategoryName);
        return ResponseEntity.ok(hasMinor);
    }
}
