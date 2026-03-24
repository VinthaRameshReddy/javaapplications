package com.medgo.provider.specifications;

import com.medgo.provider.domain.entity.HealthFacilityEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthFacilitySpecsTest {

    @SuppressWarnings("unchecked")
    @Test
    void hasAccountCodes_blankOnly_returnsConjunction() {
        Root<HealthFacilityEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        @SuppressWarnings("rawtypes")
        Path path = Mockito.mock(Path.class);
        when(root.get("accountCodes")).thenReturn(path);
        Predicate conj = Mockito.mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conj);

        Specification<HealthFacilityEntity> spec = HealthFacilitySpecs.hasAccountCodes(List.of(" "));
        Predicate out = spec.toPredicate(root, query, cb);
        assertEquals(conj, out);
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchByHfName_conjunctionOnBlank_elseLike() {
        Root<HealthFacilityEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        @SuppressWarnings("rawtypes")
        Path path = Mockito.mock(Path.class);
        when(root.get("hfName")).thenReturn(path);
        Predicate p = Mockito.mock(Predicate.class);
        when(cb.conjunction()).thenReturn(p);
        when(cb.like(any(), anyString())).thenReturn(p);
        when(cb.lower(any())).thenReturn(path);

        assertNotNull(HealthFacilitySpecs.searchByHfName(null).toPredicate(root, query, cb));
        assertNotNull(HealthFacilitySpecs.searchByHfName("abc").toPredicate(root, query, cb));
    }
}


