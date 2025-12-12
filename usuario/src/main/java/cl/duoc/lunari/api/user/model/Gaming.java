package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Información y preferencias gaming del cliente.
 *
 * Contiene datos relacionados con las preferencias de juegos del usuario,
 * perfil gamer, plataformas de streaming y géneros favoritos.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gaming {

    private String gamerTag;           // Nombre de jugador/gamertag
    private String favoriteGenre;      // Género favorito (rpg, fps, strategy, etc.)
    private String skillLevel;         // Nivel de habilidad (beginner, intermediate, advanced, pro)
    private List<String> streamingPlatforms;  // Plataformas de streaming (twitch, youtube, etc.)
    private String favoriteGames;      // Lista de juegos favoritos (comma-separated)
}
