@TypeDefs({
        @TypeDef(
                name="json-node",
                typeClass = JsonStringType.class)
})

package com.ridm.connector.domain;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;