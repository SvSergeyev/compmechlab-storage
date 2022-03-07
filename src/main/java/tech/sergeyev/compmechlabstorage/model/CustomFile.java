package tech.sergeyev.compmechlabstorage.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class CustomFile {

    @Id
    UUID id;

    String location;

    String name;

    long size;

    @Override
    public String toString() {
        return "CustomFile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
