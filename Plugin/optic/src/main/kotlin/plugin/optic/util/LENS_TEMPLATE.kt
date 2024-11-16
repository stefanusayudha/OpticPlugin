package plugin.optic.util

val LENS_TEMPLATE = """
    package io.github.stefanusayudha.optic
    
    interface Lens<Parent, Children> {
        infix operator fun <Cub> plus(lens: Lens<Children, Cub>): Lens<Parent, Cub> {
            return object : Lens<Parent, Cub> {
                override fun modify(parent: Parent, children: (Cub) -> Cub): Parent {
                    return this@Lens.modify(parent) {
                        lens.modify(this@Lens.get(parent), children)
                    }
                }

                override fun get(parent: Parent): Cub {
                    return lens.get(this@Lens.get(parent))
                }
            }
        }

        fun modify(parent: Parent, children: (Children) -> Children): Parent

        fun get(parent: Parent): Children
    }
""".trimIndent()