package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

/**
 * Información y preferencias gaming del cliente.
 *
 * Contiene datos relacionados con las preferencias de juegos del usuario,
 * perfil gamer, plataformas de streaming y géneros favoritos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Gaming {

    private String gamerTag;           // Nombre de jugador/gamertag
    private String favoriteGenre;      // Género favorito (rpg, fps, strategy, etc.)
    private String skillLevel;         // Nivel de habilidad (beginner, intermediate, advanced, pro)
    private List<String> streamingPlatforms;  // Plataformas de streaming (twitch, youtube, etc.)
    private String favoriteGames;      // Lista de juegos favoritos (comma-separated)

    @DynamoDbAttribute("gamerTag")
    public String getGamerTag() {
        return gamerTag;
    }

    public void setGamerTag(String gamerTag) {
        this.gamerTag = gamerTag;
    }

    @DynamoDbAttribute("favoriteGenre")
    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }

    @DynamoDbAttribute("skillLevel")
    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    @DynamoDbAttribute("streamingPlatforms")
    public List<String> getStreamingPlatforms() {
        return streamingPlatforms;
    }

    public void setStreamingPlatforms(List<String> streamingPlatforms) {
        this.streamingPlatforms = streamingPlatforms;
    }

    @DynamoDbAttribute("favoriteGames")
    public String getFavoriteGames() {
        return favoriteGames;
    }

    public void setFavoriteGames(String favoriteGames) {
        this.favoriteGames = favoriteGames;
    }
}
