package com.hysteryale.model.dealer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DealerProductId implements Serializable {
    @Column(name = "created_by_code")
    private String createByCode;

    @Column(name = "serial_number")
    private String serialNumber;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        DealerProductId castedObject = (DealerProductId) o;
        return createByCode.equals(castedObject.getCreateByCode()) &&
                serialNumber.equals(castedObject.getSerialNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(createByCode, serialNumber);
    }
}
