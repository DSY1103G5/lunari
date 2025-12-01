package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Información personal del cliente.
 *
 * Contiene datos personales básicos del usuario como nombre, teléfono,
 * fecha de nacimiento, biografía y foto de perfil.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Personal {

    private String firstName;
    private String lastName;
    private String phone;
    private String birthdate; // ISO 8601 format: YYYY-MM-DD
    private String bio;
    private String avatar; // URL to profile image
    private String memberSince; // Year or date when user joined (e.g., "2022")

    @DynamoDbAttribute("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @DynamoDbAttribute("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @DynamoDbAttribute("phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @DynamoDbAttribute("birthdate")
    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    @DynamoDbAttribute("bio")
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @DynamoDbAttribute("avatar")
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @DynamoDbAttribute("memberSince")
    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    /**
     * Obtiene el nombre completo del cliente.
     *
     * @return firstName + lastName
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
