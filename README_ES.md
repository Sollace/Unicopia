# ![Unicopia](https://github-production-user-asset-6210df.s3.amazonaws.com/6429283/244022981-71021d60-e698-4ca0-88ae-bfa71be39cc6.png)

[![Build Status](https://github.com/Sollace/Unicopia/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/Sollace/Unicopia/actions/workflows/gradle-build.yml)
[![Downloads](https://img.shields.io/github/downloads/Sollace/Unicopia/total.svg?color=yellowgreen)](https://github.com/Sollace/Unicopia/releases/latest)
[![Crowdin](https://badges.crowdin.net/unicopia/localized.svg)](https://crowdin.com/project/unicopia)
![](https://img.shields.io/badge/api-fabric-orange.svg)

[![en](https://img.shields.io/badge/lang-en-012169.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-d52b1e.svg)](README_RU.md)
[![cn](https://img.shields.io/badge/lang-cn-de2910.svg)](README_CN.md)

[Wiki](https://github.com/Sollace/Unicopia/wiki)

Trayendo la magia de la amistad a Minecraft!

Lo que empezó como un humilde utilidad para hacer jugando como un Unicornio un poco más inmersivo creció en un entero experiencia de conversión a Pony
que trae nuevas mecánicas, magia y experiencia al mundo de Minecraft, ¡para que realmente sientes que entraste al mundo de Equestria!

# Caracteristicas

## ¡Aprende cómo se siente jugar como tu favorito especie de Pony!

Unicornios, Pegasos, Ponys Terrestres, hasta los Changelings tienen sus propios habilidades especiales:

 - *Juega como Unicornio* y aprende a usar magia! Crea tu primer libro de hechizos y experimenta, descubriendo los diferentes hechizos que puedes
   crear y que hacen, o simplemente entierrate en la historia para aprender más del pasado de este mundo misterioso!

   Además de lanzando hechizos, como un escudo mágico para protegerte, o un rayo de magia para incinerar a tus enemigos, Unicornios también
   pueden usar teletransporte para superar obstáculos, o simplemente llegar a esos lugares difíciles de alcanzar.

 - *Juega como Pegaso* y domina los cielos! Además de la habilidad de volar, los Pegasos también pueden ejecutar Sonic Rainbooms, 
   controlar el clima por forzando las nubes dentro de jarras, y tienen más alcance y rapidez que otras razas.

 - ¡*Juega como un humilde Pony del fondo*! Ponys Terrestres son mas duros y pesados que los otros razas. También tienen la habilidad 
   de patear árboles para conseguir comida y acelerar la maduración de los cultivos. Nunca vas a tener hambre si eres un Pony Terrestre.

¿Sientes como convirtiéndote al lado oscuro?

 - *Conviértete en uno con la colmena,* y cambia de formas en cualquier cosa cuando juegas como un Changeling. Caza y deleitate en el amor 
   cosechado de otros jugadores y mobs. Algunas formas hasta pueden venir con sus propias habilidades.

 - *Abraza la noche* como un Pony Murciélago. Ponys Murciélagos tienen chillados ilimitados, pueden ver en la oscuridad, ¡y pueden volar! 
   El único inconveniente es que tienes que usar unas gafas - bien, bien padres - contra el sol, porque si no el sol quemará tus ojos. Creo que es un precio justo,
   ¿no?

### Gestiona tu Dieta

   Jugando como un Pony no solo es pateando y zapeando a otros! Como herbívoros, tus opciones de comida abren a incluir muchos artículos que los jugadores 
   no pueden comer normalmente. ¿Te da hambre? Trata unas flores del prado, ¡o un poco de heno! Escucho que las hamburguesas de heno son buenas, 
   si puedes encontrar avena.

### Pinturas ponificadas

   ¿Porque cuál Pony mod será este si _no_ tenía esto? Cada raza tiene al menos una pintura para representarles, ¡entonces muestra tu orgullo y vuela esa bandera!

   Atención: Banderas arcoiris (aun no) incluidas.

### Cosas naturales

 - El flujo de aire está (mal) simulada

   Pegasos, ¡cuidado con volar durante las tormentas! ¡Se puede volver feo ahí afuera!
   Si juegas como una especie voladora, o solo te gusta tener cosas bonitas, intenta construyendo una veleta.

   Te enseña el actual, totalmente real y no mal simulada, dirección del viento en tu mundo. Solo ten cuidado que la dirección 
   y fuerza del viento es situacional (y mal implementada), y va a ser diferente dependiendo en dónde estás y qué tan alto estás.

 - Aire caliente sube

   No, no es una mala película de Star Wars, es una mecánica actual. La arena y lava darán un impulso adicional hacia arriba a las especies voladoras. 
   El agua hace lo opuesto. Pruebalo! De verdad, no lo hagas, no quiero que te ahogues.

### Artículos mágicas y Artefactos

 - Crea y construye un santuario para el Corazon de Cristal para brindar soporte valioso a tus amigos
 - O regala brazaletes de camaradería a tus cuates no-Unicornios, para que puedan compartir en tus poderes, o lomas para que puedas 
   reír cuando te teletransportes y ellos terminan viniendo contigo involuntariamente
 - Manda y recibe artículos usando el pergamino del aliento del dragón
 - Posiblemente más que estoy olvidando (o no lo olvide? OoOoOoooOOoo… unas mecánicas de sorpresa)

Tiene comentarios en esta descripción? Encontraste problemas, o falta algo? 
Mandame DM directamente en Discord. (en inglés, si es posible.)
Las cosas todavía están cambiando, entonces esta descripción tal vez siempre estará desactualizada.

# Como Jugar

Vea el archivo “HOW_TO_PLAY.md” para más información.

# Dependencias y Actualizando

### Solo 1.19.3

Este proyecto usa atributos-de-entidad-de-alcance, que tal vez no están actualizados al tiempo de escribir esta página.
Si está actualizando para 1.19.3, puedes seguir estos pasos para asegurarte que está disponible para git:

`git clone https://github.com/Sollace/reach-entity-attributes`

`cd reach-entity-attributes`

`gradlew build publishToMavenLocal`

### Actualizando Unicopia

`git clone https://github.com/Sollace/Unicopia`

`cd Unicopia` 

`gradlew build`

Jars actualizadas están ubicadas en   /build/bin   dentro del fólder de Unicopia después de ejecutar los dos comandos arriba.
