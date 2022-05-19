package csID_tokenized.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.net.InetAddress;

@EqualsAndHashCode
@Getter
@Setter
@Data
public class AuthRequest {
        private String personalNumber;

}
