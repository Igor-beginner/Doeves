package md.brainet.doeves.verification;

import java.time.LocalDateTime;

public class VerificationDetails {
    private Integer id;
    private String code;
    private LocalDateTime expireDate;
    private Integer missingAttempts;

    public VerificationDetails() {
        this.missingAttempts = 5;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDateTime expireDate) {
        this.expireDate = expireDate;
    }

    public Integer getMissingAttempts() {
        return missingAttempts;
    }

    public void setMissingAttempts(Integer missingAttempts) {
        this.missingAttempts = missingAttempts;
    }
}
