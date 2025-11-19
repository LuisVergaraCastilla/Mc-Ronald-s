package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.MenuCategory;
import org.mc.mcronalds.model.MenuItem;
import org.mc.mcronalds.repository.MenuCategoryRepository;
import org.mc.mcronalds.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;
    
    @Autowired
    private MenuItemRepository menuItemRepository;

    // Obtener todas las categorías
    @GetMapping
    public List<MenuCategory> getAllCategories() {
        return menuCategoryRepository.findAll();
    }

    // Obtener una categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<MenuCategory> getCategoryById(@PathVariable Long id) {
        Optional<MenuCategory> category = menuCategoryRepository.findById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear una nueva categoría
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody MenuCategory category) {
        try {
            // Validaciones básicas
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El nombre de la categoría es requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar si ya existe una categoría con el mismo nombre
            Optional<MenuCategory> existingCategory = menuCategoryRepository.findByName(category.getName());
            if (existingCategory.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ya existe una categoría con este nombre");
                return ResponseEntity.badRequest().body(error);
            }
            
            MenuCategory savedCategory = menuCategoryRepository.save(category);
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear la categoría");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Actualizar una categoría existente
    @PutMapping("/{id}")
    public ResponseEntity<MenuCategory> updateCategory(@PathVariable Long id, @RequestBody MenuCategory updatedCategory) {
        return menuCategoryRepository.findById(id).map(category -> {
            category.setName(updatedCategory.getName());
            category.setDescription(updatedCategory.getDescription());
            category.setImageUrl(updatedCategory.getImageUrl());
            category.setActive(updatedCategory.isActive());
            return ResponseEntity.ok(menuCategoryRepository.save(category));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Eliminar una categoría
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            Optional<MenuCategory> category = menuCategoryRepository.findById(id);
            if (category.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Verificar si la categoría tiene items asociados
            List<MenuItem> items = menuItemRepository.findByCategory(category.get());
            if (!items.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No se puede eliminar la categoría porque tiene items asociados");
                error.put("message", "Primero elimine o mueva los items de esta categoría");
                return ResponseEntity.badRequest().body(error);
            }
            
            menuCategoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar la categoría");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener solo categorías activas
    @GetMapping("/active")
    public List<MenuCategory> getActiveCategories() {
        return menuCategoryRepository.findByActiveTrue();
    }
    
    // Obtener items de una categoría específica
    @GetMapping("/{id}/items")
    public ResponseEntity<?> getCategoryItems(@PathVariable Long id) {
        Optional<MenuCategory> category = menuCategoryRepository.findById(id);
        if (category.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<MenuItem> items = menuItemRepository.findByCategory(category.get());
        return ResponseEntity.ok(items);
    }
    
    // Activar/Desactivar categoría
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable Long id) {
        try {
            Optional<MenuCategory> categoryOpt = menuCategoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            MenuCategory category = categoryOpt.get();
            category.setActive(!category.isActive());
            MenuCategory savedCategory = menuCategoryRepository.save(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Estado de categoría actualizado");
            response.put("category", savedCategory);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar el estado de la categoría");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
