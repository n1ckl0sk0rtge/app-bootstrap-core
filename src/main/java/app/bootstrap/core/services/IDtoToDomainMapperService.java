package app.bootstrap.core.services;

import jakarta.annotation.Nonnull;

import java.util.List;

public interface IDtoToDomainMapperService <DTO, DOMAIN> {
    DOMAIN map(DTO dto);

    @Nonnull
    default List<DOMAIN> mapList(@Nonnull List<DTO> dtoList) {
        return dtoList.stream().map(this::map).toList();
    }
}
