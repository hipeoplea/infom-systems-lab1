import js from "@eslint/js";
import vue from "eslint-plugin-vue";
import globals from "globals";
import prettier from "eslint-config-prettier";

export default [
    {
        files: ["**/*.{js,mjs,cjs,vue}"],
        ignores: ["dist/**", "node_modules/**"]
    },

    js.configs.recommended,

    ...vue.configs["flat/recommended"],

    {
        languageOptions: {
            ecmaVersion: "latest",
            sourceType: "module",
            globals: {
                ...globals.browser,
                ...globals.node
            }
        },
        rules: {
            "no-unused-vars": ["warn", { argsIgnorePattern: "^_" }],
            "vue/multi-word-component-names": "off"
        }
    },

    prettier
];
