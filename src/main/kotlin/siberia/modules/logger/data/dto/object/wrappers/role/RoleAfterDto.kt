package siberia.modules.logger.data.dto.`object`.wrappers.role

import siberia.modules.rbac.data.dto.RoleOutputDto
import siberia.modules.logger.data.dto.`object`.ObjectAfterDto

class RoleAfterDto(data: RoleOutputDto, editedFields: List<String>) : ObjectAfterDto<RoleOutputDto>(data, editedFields)