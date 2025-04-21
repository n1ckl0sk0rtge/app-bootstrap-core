package app.bootstrap.core.services;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

public interface IDtoToDomainMapperService <DTO, DOMAIN> {
    @Nonnull DOMAIN mapToDomain(@Nonnull DTO dto) throws Exception;

    @Nonnull DTO mapToDto(@Nonnull DOMAIN dto) throws Exception;

    @Nonnull
    default List<DOMAIN> mapToDomain(@Nonnull List<DTO> dtoList) throws Exception {
        final List<DOMAIN> domainList = new ArrayList<>();
        for (DTO dto : dtoList) {
            domainList.add(mapToDomain(dto));
        }
        return domainList;
    }

    @Nonnull
    default List<DTO> mapToDto(@Nonnull List<DOMAIN> dtoList) throws Exception {
        final List<DTO> domainList = new ArrayList<>();
        for (DOMAIN dto : dtoList) {
            domainList.add(mapToDto(dto));
        }
        return domainList;
    }
}
