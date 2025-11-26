module.exports = {
  extends: ["stylelint-config-standard", "stylelint-config-recommended-vue"],
  rules: {
    "at-rule-no-unknown": null,
  },
  overrides: [
    {
      files: ["**/*.vue"],
      customSyntax: "postcss-html",
    },
  ],
};
