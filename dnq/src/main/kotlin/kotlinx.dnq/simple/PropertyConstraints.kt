package kotlinx.dnq.simple

import com.jetbrains.teamsys.dnq.database.PropertyConstraint
import jetbrains.exodus.database.TransientEntity
import jetbrains.exodus.database.exceptions.SimplePropertyValidationException
import jetbrains.exodus.entitystore.constraints.inRange
import jetbrains.exodus.entitystore.constraints.regexp
import jetbrains.exodus.query.metadata.PropertyMetaData
import kotlinx.dnq.XdEntity
import kotlinx.dnq.wrapper
import java.net.MalformedURLException
import java.net.URI

class PropertyConstraintBuilder<R : XdEntity, T>() {
    val constraints = mutableListOf<PropertyConstraint<T>>()
}

fun PropertyConstraintBuilder<*, String?>.regex(pattern: Regex, message: String? = null) {
    constraints.add(regexp(pattern = pattern.toPattern()).apply {
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.email(pattern: Regex? = null, message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.email().apply {
        if (pattern != null) {
            this.pattern = pattern.toPattern()
        }
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.containsNone(chars: String, message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.containsNone().apply {
        this.chars = chars
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.alpha(message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.alpha().apply {
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.numeric(message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.numeric().apply {
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.alphaNumeric(message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.alphaNumeric().apply {
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.url(message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.url().apply {
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.length(min: Int = 0, max: Int = Int.MAX_VALUE, message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.length().apply {
        if (min > 0) {
            this.min = min
        }
        if (max < Int.MAX_VALUE) {
            this.max = max
        }
        if (message != null) {
            when {
                min > 0 && max < Int.MAX_VALUE -> this.rangeMessage = message
                min > 0 -> this.minMessage = message
                max < Int.MAX_VALUE -> this.maxMessage = message
            }
        }
    })
}

fun PropertyConstraintBuilder<*, String?>.uri(message: String? = null) {
    constraints.add(object : PropertyConstraint<String?>() {
        var message = message ?: "is not a valid URI"

        override fun isValid(propertyValue: String?): Boolean {
            return if (propertyValue != null) {
                try {
                    URI(propertyValue)
                    true
                } catch (e: MalformedURLException) {
                    false
                }
            } else true
        }

        override fun getExceptionMessage(propertyName: String, propertyValue: String?) =
                "$propertyName should be valid URI but was $propertyValue"

        override fun getDisplayMessage(propertyName: String, propertyValue: String?) =
                this.message
    })
}

class RequireIfConstraint<R : XdEntity, T>(val message: String?, val predicate: R.() -> Boolean) : PropertyConstraint<T>() {
    override fun check(e: TransientEntity, pmd: PropertyMetaData, value: T): SimplePropertyValidationException? {
        @Suppress("UNCHECKED_CAST")
        return if (value == null && (e.wrapper as R).predicate()) {
            val propertyName = pmd.name
            SimplePropertyValidationException(getExceptionMessage(propertyName, value), getDisplayMessage(propertyName, value), e, propertyName)
        } else {
            null
        }
    }

    override fun isValid(value: T): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getExceptionMessage(propertyName: String?, propertyValue: T): String {
        return "Value for $propertyName is required"
    }

    override fun getDisplayMessage(propertyName: String?, propertyValue: T) = message ?: "required"
}

fun <R : XdEntity, T> PropertyConstraintBuilder<R, T>.requireIf(message: String? = null, predicate: R.() -> Boolean) {
    constraints.add(RequireIfConstraint<R, T>(message, predicate))
}

fun <T : Number?> PropertyConstraintBuilder<*, T>.min(min: Long, message: String? = null) {
    constraints.add(inRange<T>().apply {
        this.min = min
        if (message != null) {
            this.minMessage = message
        }
    })
}

fun <T : Number?> PropertyConstraintBuilder<*, T>.max(max: Long, message: String? = null) {
    constraints.add(inRange<T>().apply {
        this.max = max
        if (message != null) {
            this.maxMessage = message
        }
    })
}

/* fun PropertyConstraintBuilder<*, Long?>.past(message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.past().apply {
        if (message != null) {
            this.message = message
        }
    })
}

fun PropertyConstraintBuilder<*, Long?>.future(message: String? = null) {
    constraints.add(jetbrains.exodus.entitystore.constraints.future().apply {
        if (message != null) {
            this.message = message
        }
    })
} */
