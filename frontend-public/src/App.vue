<template>
  <router-view/>
</template>

<style>
#app {
  font-family: 'Public Sans', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

:root {
  --white: #fff;
  --blue: #1B60CB;
  --blue-2: #1159E6;
  --blue-3: #145DE6;
  --light-blue: #E7F2FF;
  --light-blue-s: #D1E0F7;
  --light-blue-1: #62B7F4;
  --light-blue-2: #5FB4F4;
  --light-blue-3: #7AA7F4;
  --light-blue-4: #76A0E0;
  --light-blue-5: #3B7BDE;
  --shadow-blue: rgba(120, 174, 236, 0.2);
  --light-1: #F2FCFD;
  --light-2: #EDF2FB;
  --dark-gray: #666973;
  --gray: #a6a6a6;
  --light-green: #28C76F;
  --orange: #FF9F43;
}

.bg-blue {
  background-color: var(--blue)!important;
}

.bg-green {
  background-color: var(--light-green)!important;
}

.text-blue {
  color: var(--blue)!important;
}

.text-gray {
  color: var(--dark-gray)!important;
}

.shadow-blue {
  box-shadow: 0 0.5rem 1rem var(--shadow-blue);
}

@media (min-width: 1400px) {
  .container, .container-lg, .container-md, .container-sm, .container-xl, .container-xxl {
    max-width: 1200px!important;
    width: 100%;
  }
}
</style>
