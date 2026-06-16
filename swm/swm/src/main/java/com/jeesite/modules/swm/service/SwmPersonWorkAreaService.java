package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.entity.SwmArea;
import com.jeesite.modules.swm.dao.SwmPersonWorkAreaDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonWorkArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SwmPersonWorkAreaService extends CrudService<SwmPersonWorkAreaDao, SwmPersonWorkArea> {

    private static final int BATCH_INSERT_SIZE = 500;

    @Autowired
    private SwmPersonService swmPersonService;

    @Autowired
    private SwmAreaService swmAreaService;

    @Override
    public SwmPersonWorkArea get(SwmPersonWorkArea swmPersonWorkArea) {
        return super.get(swmPersonWorkArea);
    }

    @Override
    @Transactional(readOnly = false)
    public Page<SwmPersonWorkArea> findPage(SwmPersonWorkArea swmPersonWorkArea) {
        dao.deleteInvalidAreaBinds();
        return super.findPage(swmPersonWorkArea);
    }

    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonWorkArea swmPersonWorkArea) {
        fillPersonAndAreaInfo(swmPersonWorkArea);
        checkDuplicate(swmPersonWorkArea);
        super.save(swmPersonWorkArea);
    }

    @Transactional(readOnly = false)
    public BatchBindResult saveBatch(SwmPersonWorkArea swmPersonWorkArea) {
        List<String> identityCardList = parseValues(firstNotBlank(
                swmPersonWorkArea.getIdentityCards(), swmPersonWorkArea.getIdentityCard()));
        List<String> areaIdList = resolveAreaIds(firstNotBlank(
                swmPersonWorkArea.getAreaIds(), swmPersonWorkArea.getAreaId()));
        return batchBind(identityCardList, areaIdList, swmPersonWorkArea.getRemarks());
    }

    @Transactional(readOnly = false)
    public BatchBindResult batchBind(List<String> identityCardList, List<String> areaIdList, String remarks) {
        dao.deleteInvalidAreaBinds();
        if (identityCardList.isEmpty()) {
            throw new IllegalArgumentException("请选择人员");
        }
        if (areaIdList.isEmpty()) {
            throw new IllegalArgumentException("请选择区域");
        }

        BatchBindResult result = new BatchBindResult();
        List<SwmPersonWorkArea> insertList = new ArrayList<>();
        for (String identityCard : identityCardList) {
            for (String areaId : areaIdList) {
                SwmPersonWorkArea bind = buildBind(identityCard, areaId, remarks);
                if (dao.countActiveSameBind(bind) > 0) {
                    result.skippedCount++;
                    continue;
                }
                bind.preInsert();
                insertList.add(bind);
                result.successCount++;
            }
        }
        insertBatch(insertList);
        result.totalCount = identityCardList.size() * areaIdList.size();
        return result;
    }

    @Transactional(readOnly = false)
    public BatchBindResult replaceBind(String identityCard, List<String> areaIdList, String remarks) {
        dao.deleteInvalidAreaBinds();
        identityCard = StringUtils.trimToEmpty(identityCard);
        if (StringUtils.isBlank(identityCard)) {
            throw new IllegalArgumentException("请选择人员");
        }
        SwmPerson person = swmPersonService.getByIdentityCard(identityCard);
        if (person == null) {
            throw new IllegalArgumentException("未找到身份证对应的人员：" + identityCard);
        }

        BatchBindResult result = new BatchBindResult();
        result.totalCount = areaIdList.size();
        if (areaIdList.isEmpty()) {
            result.deletedCount = dao.deleteAllByIdentityCard(identityCard);
            return result;
        }

        result.deletedCount = dao.deleteByIdentityCardExcludeAreaIds(identityCard, areaIdList);
        List<SwmPersonWorkArea> insertList = new ArrayList<>();
        for (String areaId : areaIdList) {
            SwmPersonWorkArea bind = buildBind(identityCard, areaId, remarks);
            if (dao.countActiveSameBind(bind) > 0) {
                result.skippedCount++;
                continue;
            }
            bind.preInsert();
            insertList.add(bind);
            result.successCount++;
        }
        insertBatch(insertList);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmPersonWorkArea swmPersonWorkArea) {
        super.updateStatus(swmPersonWorkArea);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonWorkArea swmPersonWorkArea) {
        dao.delete(swmPersonWorkArea);
    }

    public List<String> parseInputValues(String value) {
        return parseValues(value);
    }

    public List<String> resolveInputAreaIds(String areaIds) {
        return resolveAreaIds(areaIds);
    }

    private SwmPersonWorkArea buildBind(String identityCard, String areaId, String remarks) {
        SwmPersonWorkArea bind = new SwmPersonWorkArea();
        bind.setIdentityCard(identityCard);
        bind.setAreaId(areaId);
        bind.setRemarks(remarks);
        fillPersonAndAreaInfo(bind);
        return bind;
    }

    private void fillPersonAndAreaInfo(SwmPersonWorkArea swmPersonWorkArea) {
        String identityCard = StringUtils.trimToEmpty(swmPersonWorkArea.getIdentityCard());
        String areaId = StringUtils.trimToEmpty(swmPersonWorkArea.getAreaId());
        swmPersonWorkArea.setIdentityCard(identityCard);
        swmPersonWorkArea.setAreaId(areaId);

        SwmPerson person = swmPersonService.getByIdentityCard(identityCard);
        if (person == null) {
            throw new IllegalArgumentException("未找到身份证对应的人员：" + identityCard);
        }
        swmPersonWorkArea.setPersonName(person.getName());

        SwmArea area = swmAreaService.get(areaId);
        if (area == null) {
            throw new IllegalArgumentException("未找到区域：" + areaId);
        }
        swmPersonWorkArea.setAreaName(area.getAreaName());
    }

    private void checkDuplicate(SwmPersonWorkArea swmPersonWorkArea) {
        int count = dao.countActiveSameBind(swmPersonWorkArea);
        if (count > 0) {
            throw new IllegalArgumentException("该人员已绑定此工作区域，请勿重复绑定");
        }
    }

    private List<String> resolveAreaIds(String areaIds) {
        String trimmedAreaIds = StringUtils.trimToEmpty(areaIds);
        if ("ALL".equalsIgnoreCase(trimmedAreaIds) || "__all__".equalsIgnoreCase(trimmedAreaIds)) {
            SwmArea query = new SwmArea();
            query.setStatus(SwmArea.STATUS_NORMAL);
            List<SwmArea> areaList = swmAreaService.findList(query);
            List<String> result = new ArrayList<>();
            for (SwmArea area : areaList) {
                if (StringUtils.isNotBlank(area.getId())) {
                    result.add(area.getId());
                }
            }
            return result;
        }
        return parseValues(areaIds);
    }

    private List<String> parseValues(String value) {
        Set<String> values = new LinkedHashSet<>();
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        String normalized = value
                .replace(";", ",")
                .replace("\r", ",")
                .replace("\n", ",")
                .replace("\t", ",");
        for (String item : normalized.split(",")) {
            String trimmed = StringUtils.trimToEmpty(item);
            if (StringUtils.isNotBlank(trimmed)) {
                values.add(trimmed);
            }
        }
        return new ArrayList<>(values);
    }

    private String firstNotBlank(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }

    private void insertBatch(List<SwmPersonWorkArea> insertList) {
        if (insertList.isEmpty()) {
            return;
        }
        for (int start = 0; start < insertList.size(); start += BATCH_INSERT_SIZE) {
            int end = Math.min(start + BATCH_INSERT_SIZE, insertList.size());
            dao.insertBatch(insertList.subList(start, end));
        }
    }

    public static class BatchBindResult {
        private int totalCount;
        private int successCount;
        private int skippedCount;
        private int deletedCount;

        public int getTotalCount() {
            return totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public int getDeletedCount() {
            return deletedCount;
        }
    }
}
