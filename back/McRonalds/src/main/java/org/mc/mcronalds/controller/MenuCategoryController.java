package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.MenuCategory;
import org.mc.mcronalds.repository.MenuCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

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
    public MenuCategory createCategory(@RequestBody MenuCategory category) {
        return menuCategoryRepository.save(category);
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
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (menuCategoryRepository.existsById(id)) {
            menuCategoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
