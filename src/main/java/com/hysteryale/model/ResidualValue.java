package com.hysteryale.model;

import com.hysteryale.model.embedId.ResidualValueId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "residual_value")
public class ResidualValue extends BaseModel implements Serializable  {

    @EmbeddedId
    private ResidualValueId id;

    private double residualValuePercent;
    private int year;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResidualValue that = (ResidualValue) o;
        return year == that.year && Objects.equals(id.getProduct().getModelCode(), that.id.getProduct().getModelCode())
                && Objects.equals(id.getHours(), that.id.getHours());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.getProduct().getModelCode(), id.getHours(), year);
    }
}
