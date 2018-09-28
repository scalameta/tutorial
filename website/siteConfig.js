// See https://docusaurus.io/docs/site-config.html for all the possible
// site configuration options.

const repoUrl = "https://github.com/scalameta/scalameta";
const gitterUrl = "https://gitter.im/scalameta/scalameta";
// Trees.scala is not the best demo for mbrowse since the macro annotations break the navigation.
// Howevever, I can't think of a better source file in the Scalameta codebase to link to.
// We should fix semanticdb-scalac to emit occurrences for macro annotations so this won't be a problem.
const mbrowseUrl =
  "https://scalameta.org/mbrowse/#/scalameta/scalameta/trees/shared/src/main/scala/scala/meta/Trees.scala#L10C13-L10C17";

const siteConfig = {
  title: "Scalameta",
  tagline: "Library to read, analyze, transform and generate Scala programs",
  url: "https://scalameta.org",
  cname: "scalameta.org",

  baseUrl: "/",
  projectName: "scalameta.github.io",
  organizationName: "scalameta",

  // For publishing to personal organization, uncomment below:
  // baseUrl: "/scalameta/",
  // projectName: "scalameta",
  // organizationName: "olafurpg",

  algolia: {
    apiKey: "0460d1c693a278bc1ad07ada3fb96c63",
    indexName: "scalameta"
  },

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    { doc: "trees/guide", label: "Trees" },
    { doc: "semanticdb/guide", label: "SemanticDB" },
    { href: mbrowseUrl, label: "Browse sources", external: true },
    { href: repoUrl, label: "GitHub", external: true }
  ],

  // If you have users set above, you add it here:
  // users,

  /* path to images for header/footer */
  headerIcon: "img/scalameta.png",
  footerIcon: "img/scalameta.png",
  favicon: "img/favicon.png",

  /* colors for website */
  colors: {
    primaryColor: "#005124",
    secondaryColor: "#181A1F"
  },

  customDocsPath: "docs-out",

  // This copyright info is used in /core/Footer.js and blog rss/atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} Scalameta`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks
    theme: "github"
  },

  /* On page navigation for the current documentation page */
  onPageNav: "separate",

  // Disabled because some markdown files are from scalameta/tutorial and some are from scalameta/scalameta.
  // editUrl: `${repoUrl}/edit/master/docs/`,

  repoUrl,
  gitterUrl
};

module.exports = siteConfig;
