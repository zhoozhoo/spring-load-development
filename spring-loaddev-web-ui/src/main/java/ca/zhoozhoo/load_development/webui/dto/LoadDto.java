package ca.zhoozhoo.load_development.webui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class LoadDto {

    private Long id;

    @NotBlank(message = "Cartridge name is required")
    @Size(max = 100, message = "Cartridge name must not exceed 100 characters")
    private String cartridge;

    @NotBlank(message = "Bullet name is required")
    @Size(max = 100, message = "Bullet name must not exceed 100 characters")
    private String bullet;

    @NotBlank(message = "Powder name is required")
    @Size(max = 100, message = "Powder name must not exceed 100 characters")
    private String powder;

    private Double powderCharge;

    @Size(max = 100, message = "Primer name must not exceed 100 characters")
    private String primer;

    @Size(max = 100, message = "Case name must not exceed 100 characters")
    private String caseName;

    private Double overallLength;

    private Integer velocity;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    // Default constructor
    public LoadDto() {}

    // Constructor
    public LoadDto(Long id, String cartridge, String bullet, String powder, Double powderCharge,
                   String primer, String caseName, Double overallLength, Integer velocity,
                   String notes, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy) {
        this.id = id;
        this.cartridge = cartridge;
        this.bullet = bullet;
        this.powder = powder;
        this.powderCharge = powderCharge;
        this.primer = primer;
        this.caseName = caseName;
        this.overallLength = overallLength;
        this.velocity = velocity;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCartridge() {
        return cartridge;
    }

    public void setCartridge(String cartridge) {
        this.cartridge = cartridge;
    }

    public String getBullet() {
        return bullet;
    }

    public void setBullet(String bullet) {
        this.bullet = bullet;
    }

    public String getPowder() {
        return powder;
    }

    public void setPowder(String powder) {
        this.powder = powder;
    }

    public Double getPowderCharge() {
        return powderCharge;
    }

    public void setPowderCharge(Double powderCharge) {
        this.powderCharge = powderCharge;
    }

    public String getPrimer() {
        return primer;
    }

    public void setPrimer(String primer) {
        this.primer = primer;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public Double getOverallLength() {
        return overallLength;
    }

    public void setOverallLength(Double overallLength) {
        this.overallLength = overallLength;
    }

    public Integer getVelocity() {
        return velocity;
    }

    public void setVelocity(Integer velocity) {
        this.velocity = velocity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
