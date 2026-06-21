package cn.iocoder.yudao.module.swm.controller.admin.person;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员管理")
@RestController
@RequestMapping("/swm/person")
@Validated
public class SwmPersonController {

    @Resource
    private SwmPersonService personService;

    @PostMapping("/create")
    @Operation(summary = "创建人员管理")
    @PreAuthorize("@ss.hasPermission('swm:person:create')")
    public CommonResult<String> createPerson(@Valid @RequestBody SwmPersonSaveReqVO createReqVO) {
        String id = personService.createPerson(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员管理")
    @PreAuthorize("@ss.hasPermission('swm:person:update')")
    public CommonResult<Boolean> updatePerson(@Valid @RequestBody SwmPersonSaveReqVO updateReqVO) {
        personService.updatePerson(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person:delete')")
    public CommonResult<Boolean> deletePerson(@RequestParam("id") String id) {
        personService.deletePerson(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<SwmPersonRespVO> getPerson(@RequestParam("id") String id) {
        SwmPersonDO person = personService.getPerson(id);
        return success(BeanUtils.toBean(person, SwmPersonRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员管理")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<PageResult<SwmPersonRespVO>> getPersonPage(@Valid SwmPersonPageReqVO pageReqVO) {
        PageResult<SwmPersonDO> pageResult = personService.getPersonPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonRespVO.class));
    }

}
