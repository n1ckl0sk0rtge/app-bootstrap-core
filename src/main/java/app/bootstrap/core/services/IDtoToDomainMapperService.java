package app.bootstrap.core.services;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

public interface IDtoToDomainMapperService <DTO, DOMAIN> {
    @Nonnull DOMAIN map(@Nonnull DTO dto) throws Exception;

    @Nonnull
    default List<DOMAIN> map(@Nonnull List<DTO> dtoList) throws Exception {
        final List<DOMAIN> domainList = new ArrayList<>();
        for (DTO dto : dtoList) {
            domainList.add(map(dto));
        }
        return domainList;
    }
}
