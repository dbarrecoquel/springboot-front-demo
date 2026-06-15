package com.example.shippingmethod.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "delivery_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRule {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
				name = "carrier_id",
				nullable = false
			)
	private Carrier carrier;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shipping_method_id", nullable = false)
	private ShippingMethod shippingMethod;
	
	 @Column(
	            nullable = false,
	            length = 5
	    )
    private String countryCode;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean active;
}
