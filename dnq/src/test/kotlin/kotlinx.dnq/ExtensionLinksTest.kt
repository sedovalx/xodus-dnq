package kotlinx.dnq

import com.google.common.truth.Truth.assertThat
import jetbrains.exodus.database.exceptions.ConstraintsValidationException
import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.ExtensionLinksTest.Person
import kotlinx.dnq.ExtensionLinksTest.Spy
import kotlinx.dnq.util.getDBName
import org.junit.Test

class ExtensionLinksTest : DBTest() {

    class Person(override val entity: Entity) : XdEntity() {
        companion object : XdNaturalEntityType<Person>()

        var name by xdStringProp()
    }

    class Spy(override val entity: Entity) : XdEntity() {
        companion object : XdNaturalEntityType<Spy>()

        val informant by xdLink1_N(Person::curator, dbOppositePropertyName = "_curator_", dbPropertyName = "_informant_")
    }

    override fun registerEntityTypes() {
        //order is necessary
        XdModel.registerNode(Spy)
        XdModel.registerNode(Person)
    }

    @Test
    fun `setters should work`() {
        val person = store.transactional {
            Person.new {
                name = "Rozenberg"
            }
        }
        store.transactional {
            val spy = Spy.new {
                informant.add(person)
            }
            assertThat(person.curator).isEqualTo(spy)
        }
    }

    @Test(expected = ConstraintsValidationException::class)
    fun `constraints should work`() {
        store.transactional {
            Spy.new()
        }
    }


    @Test
    fun `extension links should be pushed to metadata`() {
        assertThat(Spy::informant.getDBName()).isEqualTo("_informant_")
        assertThat(Person::curator.getDBName()).isEqualTo("_curator_")
    }
}


var Person.curator: Spy? by xdLink0_1(Spy::informant, dbPropertyName = "_curator_", dbOppositePropertyName = "_informant_")