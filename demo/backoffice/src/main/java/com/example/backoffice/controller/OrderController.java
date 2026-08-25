package com.example.backoffice.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.order.dto.OrderDto;
import com.example.order.dto.OrderProductLineItemDto;
import com.example.order.model.Order;
import com.example.order.service.OrderProductLineItemService;
import com.example.order.service.OrderService;
import com.example.payment.dto.TransactionDto;
import com.example.payment.service.TransactionService;
import com.example.user.model.User;
import com.example.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/orders")
@Slf4j
public class OrderController {
	
	private final OrderService orderService;
	private final OrderProductLineItemService orderProductLineItemService;
	private final TransactionService transactionService;
	private final UserService userService;
	
	public OrderController(OrderService orderService,
							OrderProductLineItemService orderProductLineItemService,
							TransactionService transactionService,
							UserService userService) {
		
		this.orderService = orderService;
		this.orderProductLineItemService = orderProductLineItemService;
		this.transactionService = transactionService;
		this.userService = userService;
		
	}
	
	@GetMapping
	public String listOrders(@RequestParam(defaultValue = "0") int page,
							 @RequestParam(defaultValue = "10") int size,
							 @RequestParam(required = false) String search,
							 @RequestParam(required = false) String status,
							 Model model) {
		
		try {
			
		
			  Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
			  
			  Page<Order> ordersPage;
			  
			  if (search != null && !search.isEmpty()) {
				  
				  ordersPage = orderService.searchOrdersByNumber(search, pageable);
			  }
			  else if (status != null && !status.isEmpty()) {
				  
				  ordersPage = orderService.getOrdersByStatus(status, pageable);
			  }
			  else
				  ordersPage = orderService.getAllOrdersPaginated(pageable);
			  
			  model.addAttribute("orders", ordersPage.getContent());
			  model.addAttribute("currentPage", page);
			  model.addAttribute("pageSize", size);
			  model.addAttribute("totalPages", ordersPage.getTotalPages());
	          model.addAttribute("totalElements", ordersPage.getTotalElements());
	          model.addAttribute("hasNext", ordersPage.hasNext());
	          model.addAttribute("hasPrevious", ordersPage.hasPrevious());
	          model.addAttribute("search", search);
	          model.addAttribute("status", status);
	          
	          return "orders/list";
		}
		catch(Exception e) {
			model.addAttribute("error", "Erreur lors du chargement des commandes");
            return "orders/list";
		}
	}
	
	@GetMapping("/{id}")
	public String viewOrderDetail(@PathVariable Long id, Model model) {
		
		try {
			
			OrderDto order = orderService.getOrderById(id).orElseThrow(() -> new RuntimeException("Commande non trouvée"));
			
			List<OrderProductLineItemDto> orderItems = orderProductLineItemService.getOrderItems(id);
			
			User user = userService.findById(order.getUserId()).orElse(null);
			
			Optional<TransactionDto> transaction = transactionService.getTransactionByOrderId(id);
            
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("user", user);
            model.addAttribute("transaction", transaction.orElse(null));
            
            return "orders/detail";
		}
		catch (Exception e) {
			
			return "redirect:/orders";
		}
		
	}
	
	@PostMapping("/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Model model) {
        try {
            OrderDto order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
            
            orderService.updateOrderStatus(id, status);
            
            model.addAttribute("success", "Statut de la commande mis à jour");
            
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            log.error("Erreur mise à jour statut: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de la mise à jour");
            return "redirect:/orders/" + id;
        }
	}
	@GetMapping("/export/csv")
    public void exportOrdersCSV() {
        try {
            log.info("Export commandes en CSV");
        } catch (Exception e) {
            log.error("Erreur export CSV: {}", e.getMessage());
        }
    }
}
