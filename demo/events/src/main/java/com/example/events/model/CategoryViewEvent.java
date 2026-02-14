package com.example.events.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CategoryViewEvent {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("category_id")
    private Long categoryId;
    
    @JsonProperty("category_name")
    private String categoryName;
    
    @JsonProperty("parent_category_id")
    private Long parentCategoryId;
    
    @JsonProperty("parent_category_name")
    private String parentCategoryName;
    
    @JsonProperty("depth_level")
    private Integer depthLevel;
    
    @JsonProperty("subcategories_count")
    private Integer subcategoriesCount;
    
    @JsonProperty("products_count")
    private Integer productsCount;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("user_email")
    private String userEmail;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;
    
    @JsonProperty("ip_address")
    private String ipAddress;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    @JsonProperty("breadcrumb_path")
    private String breadcrumbPath;
    
    // Constructeur par défaut
    public CategoryViewEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
    }
    
    // Constructeur complet
    public CategoryViewEvent(Long categoryId, String categoryName, 
                            Long parentCategoryId, String parentCategoryName,
                            Integer depthLevel, Integer subcategoriesCount, Integer productsCount,
                            Long userId, String sessionId, String userEmail) {
        this();
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
        this.parentCategoryName = parentCategoryName;
        this.depthLevel = depthLevel;
        this.subcategoriesCount = subcategoriesCount;
        this.productsCount = productsCount;
        this.userId = userId;
        this.sessionId = sessionId;
        this.userEmail = userEmail;
    }
    
    // Getters and Setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Long getParentCategoryId() {
        return parentCategoryId;
    }
    
    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
    
    public String getParentCategoryName() {
        return parentCategoryName;
    }
    
    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }
    
    public Integer getDepthLevel() {
        return depthLevel;
    }

	public Integer getSubcategoriesCount() {
		return subcategoriesCount;
	}

	public void setSubcategoriesCount(Integer subcategoriesCount) {
		this.subcategoriesCount = subcategoriesCount;
	}

	public Integer getProductsCount() {
		return productsCount;
	}

	public void setProductsCount(Integer productsCount) {
		this.productsCount = productsCount;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getBreadcrumbPath() {
		return breadcrumbPath;
	}

	public void setBreadcrumbPath(String breadcrumbPath) {
		this.breadcrumbPath = breadcrumbPath;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}
}
