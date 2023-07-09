package com.lmeng.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmeng.yupao.model.domain.Tag;
import com.lmeng.yupao.service.TagService;
import com.lmeng.yupao.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 26816
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-06-28 17:28:58
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




