package bo.edu.uagrm.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;
}
