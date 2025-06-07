package model;

public class Member {
	
	private String accountId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String rank;
    private String status;
    
    public Member() {
    }
    
    public Member(String accountId, String fullName) {
        this.accountId = accountId;
        this.fullName = fullName;
    }
    
    public Member(String accountId, String fullName, String email, String phone, String address, String rank, String status) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.rank = rank;
        this.status = status;
    }
    
    public Member(String accountId, String fullName, String phone, String email, String address, String rank) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.rank = rank;
    }
    
    public String getAccountId() { return accountId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getRank() { return rank; }
    public String getStatus() { return status; }

    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setRank(String rank) { this.rank = rank; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return accountId;
    }
}
