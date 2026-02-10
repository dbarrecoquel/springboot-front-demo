package com.example.address.dto;

public class AddressDto {
		private String label; 
	    
	    private String street;
	    
	    private String complement;
	    
	    private String city;
	    
	    private String postalCode;
	    
	    private String country;
	    
	    private String addressType; // "BILLING" or "SHIPPING"
	    
	    private Boolean isDefault = false;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getComplement() {
			return complement;
		}

		public void setComplement(String complement) {
			this.complement = complement;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getPostalCode() {
			return postalCode;
		}

		public void setPostalCode(String postalCode) {
			this.postalCode = postalCode;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getAddressType() {
			return addressType;
		}

		public void setAddressType(String addressType) {
			this.addressType = addressType;
		}

		public Boolean getIsDefault() {
			return isDefault;
		}

		public void setIsDefault(Boolean isDefault) {
			this.isDefault = isDefault;
		}
	    
	    
}
