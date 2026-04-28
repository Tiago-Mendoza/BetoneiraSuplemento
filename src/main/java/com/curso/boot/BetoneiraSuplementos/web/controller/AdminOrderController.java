package com.curso.boot.BetoneiraSuplementos.web.controller;

import com.curso.boot.BetoneiraSuplementos.admin.dto.AdminOrderCreateForm;
import com.curso.boot.BetoneiraSuplementos.admin.dto.AdminOrderUpdateForm;
import com.curso.boot.BetoneiraSuplementos.checkout.model.StoredOrder;
import com.curso.boot.BetoneiraSuplementos.checkout.service.JsonOrderService;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class AdminOrderController {

    private static final List<String> ORDER_STATUSES = List.of(
        "CONFIRMADO",
        "PAGO",
        "EM SEPARACAO",
        "ENVIADO",
        "ENTREGUE",
        "CANCELADO"
    );

    private static final List<String> PAYMENT_METHODS = List.of(
        "Cartão de Crédito",
        "PIX",
        "Boleto"
    );

    private final JsonOrderService jsonOrderService;
    private final ProductCatalogService productCatalogService;

    public AdminOrderController(JsonOrderService jsonOrderService, ProductCatalogService productCatalogService) {
        this.jsonOrderService = jsonOrderService;
        this.productCatalogService = productCatalogService;
    }

    @GetMapping("/admin/pedidos")
    public String listarPedidos(Model model) {
        model.addAttribute("orders", jsonOrderService.findAll());
        return "admin-pedidos";
    }

    @GetMapping("/admin/pedidos/novo")
    public String novoPedido(Model model) {
        model.addAttribute("orderForm", new AdminOrderCreateForm());
        enrichCreateFormModel(model);
        model.addAttribute("pageTitle", "Novo Pedido");
        model.addAttribute("formAction", "/admin/pedidos");
        model.addAttribute("createMode", true);
        return "admin-pedido-form";
    }

    @PostMapping("/admin/pedidos")
    public String criarPedido(
        @Valid @ModelAttribute("orderForm") AdminOrderCreateForm form,
        BindingResult bindingResult,
        Model model
    ) {
        validateAddressFields(form.getZipCode(), form.getState(), bindingResult);

        if (bindingResult.hasErrors()) {
            enrichCreateFormModel(model);
            model.addAttribute("pageTitle", "Novo Pedido");
            model.addAttribute("formAction", "/admin/pedidos");
            model.addAttribute("createMode", true);
            return "admin-pedido-form";
        }

        jsonOrderService.createAdminOrder(form);
        return "redirect:/admin/pedidos?created";
    }

    @GetMapping("/admin/pedidos/{orderNumber}")
    public String editarPedido(@PathVariable String orderNumber, Model model) {
        StoredOrder order = jsonOrderService.getRequiredByOrderNumber(orderNumber);
        AdminOrderUpdateForm updateForm = mapToUpdateForm(order);

        model.addAttribute("order", order);
        model.addAttribute("orderForm", updateForm);
        model.addAttribute("orderStatuses", ORDER_STATUSES);
        model.addAttribute("paymentMethods", PAYMENT_METHODS);
        model.addAttribute("pageTitle", "Editar Pedido");
        model.addAttribute("formAction", "/admin/pedidos/" + orderNumber + "/atualizar");
        model.addAttribute("createMode", false);
        return "admin-pedido-form";
    }

    @PostMapping("/admin/pedidos/{orderNumber}/atualizar")
    public String atualizarPedido(
        @PathVariable String orderNumber,
        @Valid @ModelAttribute("orderForm") AdminOrderUpdateForm form,
        BindingResult bindingResult,
        Model model
    ) {
        validateAddressFields(form.getZipCode(), form.getState(), bindingResult);
        StoredOrder order = jsonOrderService.getRequiredByOrderNumber(orderNumber);

        if (bindingResult.hasErrors()) {
            model.addAttribute("order", order);
            model.addAttribute("orderStatuses", ORDER_STATUSES);
            model.addAttribute("paymentMethods", PAYMENT_METHODS);
            model.addAttribute("pageTitle", "Editar Pedido");
            model.addAttribute("formAction", "/admin/pedidos/" + orderNumber + "/atualizar");
            model.addAttribute("createMode", false);
            return "admin-pedido-form";
        }

        jsonOrderService.updateOrder(orderNumber, form);
        return "redirect:/admin/pedidos?updated";
    }

    @PostMapping("/admin/pedidos/{orderNumber}/remover")
    public String removerPedido(@PathVariable String orderNumber) {
        jsonOrderService.deleteOrder(orderNumber);
        return "redirect:/admin/pedidos?deleted";
    }

    private void enrichCreateFormModel(Model model) {
        model.addAttribute("orderStatuses", ORDER_STATUSES);
        model.addAttribute("paymentMethods", PAYMENT_METHODS);
        model.addAttribute("products", productCatalogService.getAllProducts());
    }

    private AdminOrderUpdateForm mapToUpdateForm(StoredOrder order) {
        AdminOrderUpdateForm form = new AdminOrderUpdateForm();
        form.setCustomerEmail(order.getCustomerEmail());
        form.setRecipientName(order.getRecipientName());
        form.setZipCode(order.getZipCode());
        form.setStreet(order.getStreet());
        form.setNumber(order.getNumber());
        form.setComplement(order.getComplement());
        form.setNeighborhood(order.getNeighborhood());
        form.setCity(order.getCity());
        form.setState(order.getState());
        form.setPaymentMethod(order.getPaymentMethod());
        form.setStatus(order.getStatus());
        return form;
    }

    private void validateAddressFields(String zipCode, String state, BindingResult bindingResult) {
        String normalizedZipCode = digitsOnly(zipCode);
        if (!normalizedZipCode.isBlank() && normalizedZipCode.length() != 8) {
            bindingResult.rejectValue("zipCode", "invalid", "Informe um CEP com 8 dígitos.");
        }

        String normalizedState = state == null ? "" : state.trim();
        if (!normalizedState.isBlank() && normalizedState.length() < 2) {
            bindingResult.rejectValue("state", "invalid", "Informe um estado válido.");
        }
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
