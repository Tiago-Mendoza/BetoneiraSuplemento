package com.curso.boot.BetoneiraSuplementos.web.controller;

import com.curso.boot.BetoneiraSuplementos.admin.dto.AdminProductForm;
import com.curso.boot.BetoneiraSuplementos.store.model.Product;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminProductController {

    private final ProductCatalogService productCatalogService;

    public AdminProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping("/admin/produtos")
    public String listarProdutos(Model model) {
        model.addAttribute("products", productCatalogService.getAllProducts());
        return "admin-produtos";
    }

    @GetMapping("/admin/produtos/novo")
    public String novoProduto(Model model) {
        model.addAttribute("productForm", new AdminProductForm());
        model.addAttribute("pageTitle", "Novo produto");
        model.addAttribute("formAction", "/admin/produtos");
        model.addAttribute("editMode", false);
        return "admin-produto-form";
    }

    @PostMapping("/admin/produtos")
    public String criarProduto(
        @Valid @ModelAttribute("productForm") AdminProductForm form,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Novo produto");
            model.addAttribute("formAction", "/admin/produtos");
            model.addAttribute("editMode", false);
            return "admin-produto-form";
        }

        productCatalogService.createProduct(form);
        return "redirect:/admin/produtos?created";
    }

    @GetMapping("/admin/produtos/{productId}")
    public String editarProduto(@PathVariable String productId, Model model) {
        Product product = productCatalogService.getRequiredProduct(productId);

        model.addAttribute("product", product);
        model.addAttribute("productForm", mapToForm(product));
        model.addAttribute("pageTitle", "Editar produto");
        model.addAttribute("formAction", "/admin/produtos/" + productId + "/atualizar");
        model.addAttribute("editMode", true);
        return "admin-produto-form";
    }

    @PostMapping("/admin/produtos/{productId}/atualizar")
    public String atualizarProduto(
        @PathVariable String productId,
        @Valid @ModelAttribute("productForm") AdminProductForm form,
        BindingResult bindingResult,
        Model model
    ) {
        Product product = productCatalogService.getRequiredProduct(productId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Editar produto");
            model.addAttribute("formAction", "/admin/produtos/" + productId + "/atualizar");
            model.addAttribute("editMode", true);
            return "admin-produto-form";
        }

        productCatalogService.updateProduct(productId, form);
        return "redirect:/admin/produtos?updated";
    }

    @PostMapping("/admin/produtos/{productId}/remover")
    public String removerProduto(@PathVariable String productId) {
        productCatalogService.deleteProduct(productId);
        return "redirect:/admin/produtos?deleted";
    }

    private AdminProductForm mapToForm(Product product) {
        AdminProductForm form = new AdminProductForm();
        form.setName(product.getName());
        form.setSubtitle(product.getSubtitle());
        form.setWeight(product.getWeight());
        form.setPrice(product.getPrice());
        form.setImageFileName(product.getImageFileName());
        form.setAltText(product.getAltText());
        form.setFeatured(product.isFeatured());
        return form;
    }
}
